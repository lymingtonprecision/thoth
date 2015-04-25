(ns thoth.components.quote.view
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.data :refer [diff]]
            [cljs.core.async :refer [close! <!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponentmethod]]
            [om-tools.dom :as dom :include-macros true]
            [thoth.routes :refer [defroute]]
            [thoth.services.quotes :refer [request-updated-end-dates]]))

(defn set-latest-from-update [owner update]
  (let [data-key (if (= :ok (first update)) :latest :error)
        new-state {data-key (second update) :update nil}]
    (om/update-state! owner #(merge % new-state))))

(defn update-from-latest
  "Updates quote `q` from the `:latest` data present in `owner`,
  swapping the `:data` and recording a `:history` entry."
  [owner q]
  (if-let [latest (om/get-state owner :latest)]
    (do
      (om/set-state! owner :latest nil)
      (om/transact! q (fn [q]
                        (let [d (diff (:data q) latest)
                              h (conj (:history q) d)]
                          (assoc q :data latest :history h)))))))

(defmulti quote-view (fn [q owner] (:status q)))

(defcomponentmethod quote-view :created
  [{:keys [id part data history] :as q} owner]
  (init-state
    [_]
    {:latest nil
     :error nil
     :update nil})
  (will-receive-props
    #_"Make a request for new end dates whenever the quote data changes."
    [_ {:keys [new-data]}]
    (let [qs (om/get-shared owner :quote-service)
          ou (om/get-state owner :update)
          nu (request-updated-end-dates qs new-data)]
      (when ou (close! ou))
      (om/set-state! owner :update nu)
      (go (if-let [r (<! nu)] (set-latest-from-update owner r)))))
  (render-state
    #_"Display header with current best end date (from `data`),
      latest best end date based on changes (from `latest`),
      a \"spinner\" or other \"working\" indicator if `update` isn't nil,
      and a button to \"commit\" the changes which calls
      `update-from-latest`.

      Display quote body based on `data` any edits, sourcing, change to
      method, etc. should apply to `data` and so that the
      `will-receive-props` `fn` is called."
    [_ {:keys [latest update error]}]
    (dom/div (dom/p (str "Viewing quote " (:id q)))
             (dom/code (pr-str (:data q))))))

(defcomponentmethod quote-view :retrieving
  [q owner]
  (render
    [_]
    (dom/p "Waiting ... waiting ... waiting ...")))

(defcomponentmethod quote-view :retrieval-failed
  [q owner]
  (render
    [_]
    (dom/p (str "Failed to retrieve quote for " (-> q :part :id)))))

(defroute
  :quote ["quotes/" :id]
  (fn [cursor {:keys [id]}]
    (om/transact!
      cursor
      (fn [s]
        (assoc s :page-fn #(let [q (-> % :quotes (get id))]
                             (om/build quote-view q)))))))
