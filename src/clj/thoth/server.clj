(ns thoth.server
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]

            [compojure.core :as compojure :refer [GET]]
            [compojure.route :refer [resources]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]

            [thoth.pages.app-page :refer [app-page]]))

(defn routes [dev-mode?]
  (compojure/routes
    (resources "/")
    (GET "/*" req (app-page dev-mode?))))

(def dev-routes (routes true))
(def prod-routes (routes false))

(def default-middleware site-defaults)

(defn handler [dev-mode?]
  (if dev-mode?
    (reload/wrap-reload (wrap-defaults #'dev-routes default-middleware))
    (wrap-defaults prod-routes default-middleware)))

(defn default-port [dev-mode?] (if dev-mode? 10555 0))

(defn url [s]
  (str "http://localhost:" (:port s)))

(defrecord Server [env]
  component/Lifecycle
  (start [this]
    (if (:instance this)
      this
      (let [dev-mode? (:dev-mode? env)
            port (get env :port (default-port dev-mode?))
            h (handler dev-mode?)
            s (run-server h {:port port})]
        (assoc this
               :dev-mode? (:dev-mode? env)
               :handler (handler (:dev-mode? env))
               :instance s
               :port (:local-port (meta s))))))
  (stop [this]
    (if-let [s (:instance this)]
      (do
        (s)
        (dissoc this :instance :port :handler))
      this)))

(defn server []
  (component/using
    (map->Server {})
    [:env]))
