(ns thoth.navigation
  (:require-macros [cljs.core.async.macros :refer [go-loop]]
                   [schema.core :refer [defschema]])
  (:require [cljs.core.async :refer [chan sliding-buffer sub map> <!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om.core :as om]
            [schema.core :as s :include-macros true]
            [thoth.routes :refer [match-route path-for]])
  (:import [goog.history Html5History]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema

(defschema NavigationRequest
  "A map specifying the internal app path to navigate to and, optionally,
  the title to set in the browser when doing so. "
  {:path s/Str
   (s/optional-key :title) (s/maybe s/Str)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Our browser history instance

(def history
  "A HTML5 History instance, for managing app history/navigation"
  (doto (Html5History.)
    (.setUseFragment false)
    (.setPathPrefix "")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(s/defn navigate!
  "Change the current browser location and title to those of the
  provided `NavigationRequest`."
  [{:keys [path title]} :- NavigationRequest]
  (. history (setToken path title)))

(defn transition-to [root-cursor uri]
  (let [r (or (match-route uri)
              (match-route "/error/404"))
        h (:handler r)
        p (assoc (get r :route-params {}) :path uri)]
    (if h
      (h root-cursor p)
      (.log js/console (str "WARNING: no matching handler fn for " uri)))))

(defn transition-on-navigate!
  "Given an app cursor registers a browser navigation event listener
  that will transition the app on change of the location.

  If an invalid path is navigated to then the `:err-not-found` handler
  will be called."
  [root-cursor]
  (goog.events/listen
    history
    EventType/NAVIGATE
    (fn [e] (transition-to root-cursor (.-token e))))
  ;; we delay enabling the history till now so that we can capture the
  ;; initial navigate event for the starting URI
  (.setEnabled history true))

(defn subscribe-to-messages!
  "Subscribes to navigation events posted by components of the Om root
  `app-root`, transitioning the app to the newly navigated to address."
  [app-root]
  (let [msg-pub (om/get-shared app-root :message-pub)
        rc (om/get-props app-root)
        nav-chan (chan (sliding-buffer 1))
        nav-sub (sub msg-pub :navigate-to (map> second nav-chan))]
    (transition-on-navigate! rc)
    (go-loop
      []
      (if-let [e (<! nav-chan)]
      (do (navigate! e)
          (recur))))))
