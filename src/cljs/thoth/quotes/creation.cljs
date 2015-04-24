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
  map.

  As much as the quote data will be missing an asynchronous request for
  the data will be submitted to `quote-service` with the results channel
  of the request stored under the quotes `:updates` key."
  [quote-service part]
  (let [id (generate-id)
        data-chan (request-quote-for-part quote-service (:id part))]
    {:id id
     :part part
     :created (t/now)
     :data nil
     :updates data-chan
     :error nil
     :history []}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data processing

(defn update-quote-on-data-receipt!
  "Creates a `go` block waiting for the results on a quotes `:updates`
  channel, updating the quote data when the results arrive."
  [cursor quote-id]
  (let [data-chan (-> cursor om/state deref (get-in [:quotes quote-id :updates]))]
    (go (if-let [r (<! data-chan)]
          (let [[data error] (if (= :ok (first r))
                               [(second r) nil]
                               [nil (second r)])]
            (om/transact!
              cursor
              [:quotes quote-id]
              (fn [q]
                (assoc q :data data :error error :updates nil))))))))

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
