(ns thoth.quotes.creation
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [schema.core :refer [defschema]])
  (:require [cljs.core.async :refer [chan sub map> <! put! muxch*]]
            [cljs-time.core :as t]
            [cljs-time.coerce :as tc]
            [cljsjs.hashids]
            [om.core :as om]
            [thoth.routes :refer [path-for]]
            [thoth.services.quotes :refer [request-quote-for-part]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ID generation

(def hashid (js/Hashids.))

(defn generate-id
  "Returns a new random ID for a quote."
  []
  (.encode hashid (tc/to-long (t/now))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Quote record generation

(defn create-sparse-quote-for-part
  "Returns a sparsely populated (lacking the actual quote data) quote
  map. The returned quote will have a `:status` of `:retrieving`.

  As much as the quote data will be missing an asynchronous request for
  the data will be submitted to `quote-service` with the results channel
  of the request stored under the quotes `:details` key."
  [quote-service part]
  (let [id (generate-id)
        data-chan (request-quote-for-part quote-service (:id part))]
    {:id id
     :part part
     :created (t/now)
     :status :retrieving
     :details data-chan}))

(defn create-full-quote
  "Given a sparse quote and quote data returns a fully populated version
  of the quote with a status of `:created`."
  [sparse-quote data]
  (assoc (select-keys sparse-quote [:id :part :created])
         :status :created
         :data data
         :history []))

(defn create-errored-quote
  "Given a sparse quote and a retrieval error returns a version of the
  quote containing the error details with a `:status` of
  `:retrieval-failed`."
  [sparse-quote error]
  (assoc (select-keys sparse-quote [:id :part :created])
         :status :retrieval-failed
         :error error))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data processing

(defn update-quote-on-data-receipt!
  "Creates a `go` block waiting for the results on a quotes `:details`
  channel, updating the quote data when the results arrive."
  [cursor quote-id]
  (let [data-chan (-> cursor om/state deref (get-in [:quotes quote-id :details]))]
    (go (if-let [r (<! data-chan)]
          (om/transact!
            cursor
            [:quotes quote-id]
            (fn [q]
              (if (= :ok (first r))
                (create-full-quote q (second r))
                (create-errored-quote q (second r)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn subscribe-to-messages!
  "Subscribes to requests for creation of new quotes posted to the Om
  app, `app-root`, message channel.

  Creates the requested quotes and transitions the app to view them."
  [app-root]
  (let [msg-pub (om/get-shared app-root :message-pub)
        msg-chan (om/get-shared app-root :message-chan)
        qs (om/get-shared app-root :quote-service)
        rc (om/get-props app-root)
        c (chan)
        s (sub msg-pub :create-quote (map> second c))]
    (go-loop
      []
      (if-let [q (<! c)]
         (do (let [q (create-sparse-quote-for-part qs (:part q))]
               (om/transact! rc :quotes (fn [qs] (assoc qs (:id q) q)))
               (update-quote-on-data-receipt! rc (:id q))
               (put! msg-chan [:navigate-to {:path (path-for :quote :id (:id q))}]))
             (recur))))))
