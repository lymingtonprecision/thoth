(ns thoth.system
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [thoth.server :refer [server]]))

(defn system
  ([] (system env))
  ([env]
   (component/system-map
     :env env
     :server (server))))

(defn start [s]
  (component/start s))

(defn stop [s]
  (component/stop s))
