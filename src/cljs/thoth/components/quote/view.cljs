(ns thoth.components.quote.view
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [chan <!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [thoth.routes :refer [defroute]]))

(defcomponent quote-view [q owner]
  (render
    [_]
    (dom/div
      (if (and (nil? (:data q)) (:updates q))
        (dom/p "Waiting ... waiting ... waiting ...")
        [(dom/p (str "Viewing quote " (:id q)))
         (dom/code (pr-str (:data q)))]))))

(defroute
  :quote ["quotes/" :id]
  (fn [cursor {:keys [id]}]
    (om/transact!
      cursor
      (fn [s]
        (assoc s :page-fn #(let [q (-> % :quotes (get id))]
                             (om/build quote-view q)))))))

(comment
  (defcomponent quote-view [{:keys [id data dirty? history] :as q} owner]
    (init-state
      [_]
      {:latest nil
       :requests 0
       :updates (chan)})
    (will-mount
      "Create a `go` block on our `:updates` channel to update our
      `:latest` state with the received updates and decrement our
      `:requests` counter."
      [this]
      (go-loop
        []
        (if-let [u (<! (om/get-state owner :updates))]
          (om/update-state! owner :requests #(max 0 (dec %)))
          (if (= :ok (first u))
            (om/set-state! owner :latest (second u))
            (om/set-state! owner :error (rest u))))))
    (will-receive-props
      "Make a request for new end dates whenever the quote data changes
      without being clean."
      [_ {:keys [id data dirty?]}]
      (if dirty?
        (let [qs (om/get-shared owner :quote-service)]
          (quotes/request-updated-end-dates qs data (om/get-state owner :updates))
          (om/update-state! owner :requests inc))))
    (render-state
      "Display header with current best end date (from `data`),
      latest best end date based on changes (from `latest`),
      a \"spinner\" or other \"working\" indicator if `(> requests 0)`,
      and a button to \"commit\" the changes which replaces `data`
      with `latest` and records a suitable `history` entry.)

      Display quote body based on `data` any edits, sourcing, change to
      method, etc. should apply to `data` and trigger `(inc :requests)`
      and `(quotes/request-updated-end-dates)`"
      [_ {:keys [latest requests]}]
      (dom/p "quote"))))
