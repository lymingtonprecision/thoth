(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]

            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]

            [net.cgrand.reload :refer [auto-reload]]
            [clojurescript-build.auto :refer [stop-autobuild!]]
            [figwheel-sidecar.auto-builder :as fig-auto]
            [figwheel-sidecar.core :as fig]

            [thoth.system :as sys]
            [thoth.server :refer [url]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Figwheel auto-builder and Enlive namespace reloads

(defrecord FigwheelAutoBuilder []
  component/Lifecycle
  (start [this]
    (if (:builder this)
      this
      (let [server (fig/start-server)
            config {:figwheel-server server
                    :builds
                    [{:source-paths ["src/cljs" "env/dev"]
                      :compiler
                      {:output-to            "resources/public/js/app.js"
                       :output-dir           "resources/public/js/out"
                       :source-map           "resources/public/js/out.js.map"
                       :source-map-timestamp true
                       :preamble             ["react/react.min.js"]}}]}]
        (assoc this
               :server server
               :builder (fig-auto/autobuild* config)))))
  (stop [this]
    (if-let [b (:builder this)]
      (do
        (stop-autobuild! b)
        (fig/stop-server (:server this))
        (dissoc this :builder :server))
      this)))

(defn set-server-ns-auto-reloads []
  (doseq [n (filter #(re-find #"^thoth\\.(server|pages)" (str %)) (all-ns))]
    (auto-reload n)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dev system

(def system nil)

(defn init []
  (set-server-ns-auto-reloads)
  (alter-var-root #'system
                  (constantly
                   (assoc (sys/system env)
                          :auto-builder (map->FigwheelAutoBuilder {})))))

(defn start []
  (alter-var-root #'system sys/start))

(defn stop []
  (alter-var-root #'system sys/stop))

(defn go []
  (init)
  (start)
  (println "Running at" (url (:server system))))

(defn reset []
  (stop)
  (refresh :after 'user/go))
