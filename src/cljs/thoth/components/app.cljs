(ns thoth.components.app
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [thoth.routes :refer [defroute path-for]]
            [thoth.navigation :refer [navigate!]]
            [thoth.navigation.util :refer [navigate-to on-click-navigate]]

            [thoth.components.errors.not-found]
            [thoth.components.part-selector]
            [thoth.components.quote.list]
            [thoth.components.quote.view]))

(defroute
  :index ""
  (fn [cursor _]
    (navigate! {:path (path-for :part-selection)})))

(defcomponent quote-app [state owner]
  (render
    [_]
    (dom/div
      (dom/h2 "Due Date Quoting")
      (dom/ul
        (dom/li (dom/a (navigate-to owner :index) "Index"))
        (dom/li (dom/a (navigate-to owner :part-selection) "New Quote"))
        (dom/li (dom/a (navigate-to owner :quotes) "Quotes"))
        (dom/li (dom/a (navigate-to owner :quote :id 1) "Quote 1"))
        (dom/li (dom/a {:href "/invalid-page"
                        :on-click (on-click-navigate owner)}
                       "Not Found")))
      (if-let [pfn (:page-fn state)]
        (pfn state)))))
