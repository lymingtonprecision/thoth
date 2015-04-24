(ns thoth.components.quote.list
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [thoth.routes :refer [defroute]]
            [thoth.navigation.util :refer [navigate-to]]))

(defcomponent quote-list [quotes owner]
  (render
    [_]
    (dom/div
      (dom/h5 "Quote List")
      (dom/ul
        (map (fn [[id q]]
               (dom/li (dom/a (navigate-to owner :quote :id id) id))) quotes)))))

(defroute
  :quotes "quotes"
  (fn [cursor _]
    (om/update! cursor :page-fn (fn [state]
                                  (om/build quote-list (:quotes state))))))
