(ns thoth.components.part-selector
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [close! put!]]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]

            [thoth.services.part-lookup :refer [parts-like]]
            [thoth.routes :refer [defroute]]))

(defn update-from-search-results [owner r]
  (let [[parts error] (if (= :ok (first r))
                        [(-> r second :parts) nil]
                        [nil (second r)])]
    (om/update-state! owner #(assoc % :parts parts :error error :search-results nil))))

(defn replace-results-chan [owner s]
  (let [pl (om/get-shared owner :part-lookup)
        cc (om/get-state owner :search-results)
        nc (parts-like pl s)]
    (om/set-state! owner :search-results nc)
    (go (if-let [r (<! nc)] (update-from-search-results owner r)))
    (when cc (close! cc))))

(defn change-search [owner e]
  (let [s (.. e -target -value)]
    (if (seq s)
      (replace-results-chan owner s)
      (om/update-state! owner #(assoc % :parts [] :waiting false)))
    (om/set-state! owner :search-string s)))

(defn select-part-for-quote [owner part]
  (let [>msg (om/get-shared owner :message-chan)]
    (put! >msg [:create-quote {:part part}])))

(defcomponent part-list-entry [part owner]
  (render
    [_]
    (dom/li
      {:on-click (fn [e]
                   (select-part-for-quote owner part)
                   (.-stopPropogation e))}
      (dom/span (:id part))
      (dom/span (:description part)))))

(defcomponent part-selector [_ owner]
  (init-state
    [_]
    {:search-string ""
     :parts []
     :search-results nil
     :error nil})
  (render-state
    [_ {:keys [search-string parts error search-results]}]
    (dom/div
      (dom/input {:type "search"
                  :placeholder "part number/description"
                  :value search-string
                  :on-change #(change-search owner %)})
      (if search-results
        (dom/p "waiting for results"))
      (if error
        (dom/div {:class "error"} (dom/p (:error error)))
        (if (seq parts)
          (dom/ul (om/build-all part-list-entry parts))
          (dom/p "No results yet"))))))

(defroute
  :part-selection "quote/entry"
  (fn [cursor _]
    (om/transact! cursor #(assoc % :page-fn (fn [_] (om/build part-selector nil))))))
