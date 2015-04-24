(ns thoth.navigation.util
  (:require-macros [schema.core :refer [defschema]])
  (:require [cljs.core.async :refer [put!]]
            [schema.core :as s :include-macros true]
            [om.core :as om]
            [thoth.routes :refer [match-route path-for]]
            [thoth.navigation :refer [NavigationRequest]])
  (:import [goog Uri]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DOM utility fns

(s/defn dom-a->path :- NavigationRequest
  "Returns a `{:path ... :title ...}` map from a DOM `a` link element
  where `:path` maps to the elements `href` attribute and `:title` its
  `title`."
  [t]
  {:path (.getPath (.parse Uri (.-href t)))
   :title (.-title t)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn on-click-navigate
  "Returns an on-click handler fn that posts navigation requests to the
  `:navigation-chan` shared with `owner` where the navigation request
  will be a map of the `{:path <element href> :title <element title>}`
  of the on-clicks target element."
  [owner]
  (fn [e]
    (let [>msg (:message-chan (om/get-shared owner))]
      (put! >msg [:navigate-to (dom-a->path (.-target e))])
      (.preventDefault e))))

(defn navigate-to
  "Returns a DOM element attribute map containing the `:href` for the
  internal `path` with an `:on-click` for dispatching the internal app
  navigation."
  [owner & path]
  {:href (apply path-for path) :on-click (on-click-navigate owner)})
