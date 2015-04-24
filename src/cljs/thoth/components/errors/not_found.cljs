(ns thoth.components.errors.not-found
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [thoth.routes :refer [defroute]]))

(defcomponent not-found [_ owner]
  (render-state
    [_ {:keys [path] :or {path (-> js/document .-location .-pathname)}}]
    (dom/div
      (dom/h2 "Page Not Found")
      (dom/p
        (dom/code path)
        " isn't a page on this site."))))

(defroute
  :err-not-found "error/404"
  (fn [cursor {:keys [path]}]
    (om/update!
      cursor :page-fn
      (fn [s]
        (om/build not-found nil {:init-state {:path path}})))))
