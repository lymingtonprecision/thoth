(ns thoth.main
  (:require [figwheel.client :as figwheel :include-macros true]
            [thoth.core :as core]
            [thoth.fakes.part-lookup :refer [fake-part-lookup]]
            [thoth.fakes.quote-service :refer [fake-quote-service]]))

(defn create-services []
  {:part-lookup (fake-part-lookup
                  {:id "100105001R03"
                   :customer-part "D5303"
                   :customer-issue "AD"
                   :description "THREAD PROTECTOR, PD900RSD COLLAR, HARDFACED"}
                  {:id "100101042R01"
                   :customer-part "D5041"
                   :customer-issue "AB"
                   :description "LOCK PLATE 12.5 DEG M8 FERRY CAP SCREWS"}
                  {:id "100113301R07"
                   :customer-part "100769844"
                   :customer-issue "AM"
                   :description "BIAS UNIT BODY, 26\" PDX5"})
   :quote-service (fake-quote-service)})

(defn load-app-main []
  (core/main (create-services)))

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback load-app-main)

(load-app-main)
