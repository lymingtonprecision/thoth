(defproject thoth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3058" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ; system
                 [com.stuartsierra/component "0.2.3"]
                 [environ "1.0.0"]

                 ; http serving
                 [http-kit "2.1.19"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.4"]
                 [enlive "1.1.5"]
                 [compojure "1.3.2"]

                 ; clojurescript deps
                 [org.omcljs/om "0.8.8"]
                 [prismatic/om-tools "0.3.11"]
                 [prismatic/schema "0.4.0"]
                 [bidi "1.18.10"]
                 [com.andrewmcveigh/cljs-time "0.3.3"]
                 [com.cognitect/transit-cljs "0.8.207"]
                 [cljsjs/hashids "1.0.2-0"]
                 [cljsjs/d3 "3.5.5-3"]]

  :plugins [[lein-environ "1.0.0"]
            [lein-cljsbuild "1.0.5"]]

  :source-paths ["src/clj"]

  :cljsbuild
  {:builds
   {:app {:source-paths ["src/cljs"]
          :compiler {:output-to     "resources/public/js/app.js"
                     :output-dir    "resources/public/js/out"
                     :source-map    "resources/public/js/out.js.map"
                     :preamble      ["react/react.min.js"]
                     :optimizations :none
                     :pretty-print  true}}}}

  :clean-targets ^{:protect false} ["resources/public/js"]

  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [; figwheel for auto-reload on change in browser
                        [clojurescript-build "0.1.5"]
                        [figwheel "0.2.5"]
                        [figwheel-sidecar "0.2.5"]]

         :plugins [[lein-figwheel "0.2.5"]]

         :env {:dev-mode? true}

         :figwheel {:http-server-root "public"
                    :server-port 3449}

         :repl-options {:init-ns user}

         :cljsbuild {:builds {:app {:source-paths ["src/cljs" "env/dev"]}}}}

   :uberjar {:env {:production true}
             :omit-source true
             :aot :all
             :cljsbuild {:builds {:app {:source-paths ["env/prod"]
                                        :compiler {:optimizations :advanced
                                                   :pretty-print  false}}}}}})
