(ns thoth.fakes.quotes.thread-protector
  (:require [cljs-time.core :as t]))

(defn thread-protector-quote
  ([] (thread-protector-quote (t/today)))
  ([start-date]
   {:id "100105001R03"
    :type :structured
    :best-end-date nil
    :struct-in-use "m1*"
    :structs
    {"m1*"
     {:id {:alternative "*" :revision 1 :type :manufactured}
      :description "4330 100679220"
      :route-in-use "m7*"
      :components {"100116998R01"
                   {:id "100116998R01"
                    :customer-part nil
                    :issue nil
                    :description "4330V to 100679220 Stainless Steel Bored Billet - 10in outside diameter x 8in inside diameter x 8.25in length"
                    :type :raw
                    :lead-time 17
                    :best-end-date (t/plus start-date (t/days 17))}}
      :routes
      {"m7*"
       {:id {:type :manufactured :alternative "*" :revision 7}
        :description "IN HOUSE"
        :best-end-date (t/plus start-date (t/days 80))
        :total-buffer 68.85060292850991
        :total-touch-time 89268
        :ccr {:id "MC041"
              :operation 10
              :total-touch-time 1866
              :pre-ccr-buffer 0
              :post-ccr-buffer 63.01935292850992}
        :operations [{:id 10
                      :description "CNC Lathe"
                      :touch-time 600
                      :work-center {:id "MC041"
                                    :description "MAZAK 3M SLANT TURN"
                                    :type :internal
                                    :hours-per-day 8M
                                    :potential-ccr? true}}
                     {:id 20
                      :description "CNC Lathe"
                      :touch-time 600
                      :work-center {:id "MC041"
                                    :description "MAZAK 3M SLANT TURN"
                                    :type :internal
                                    :hours-per-day 8M
                                    :potential-ccr? true}}
                     {:id 30
                      :description "Fabrication Shop"
                      :touch-time 855
                      :work-center {:id "PR008"
                                    :description "FABRICATION SHOP"
                                    :type :internal
                                    :hours-per-day 8.1M
                                    :potential-ccr? false}}
                     {:id 40
                      :description "CNC Lathe"
                      :touch-time 666
                      :work-center {:id "MC041"
                                    :description "MAZAK 3M SLANT TURN"
                                    :type :internal
                                    :hours-per-day 8M
                                    :potential-ccr? true}}
                     {:id 50
                      :description "Part Marking"
                      :touch-time 48
                      :work-center {:id "PR004"
                                    :description "PART MARKING (IN INSPECTION)"
                                    :type :internal
                                    :hours-per-day 8.1M
                                    :potential-ccr? false}}
                     {:id 60
                      :description "Inspect"
                      :touch-time 39
                      :work-center {:id "IN000"
                                    :description "INSP - MANUAL"
                                    :type :internal
                                    :hours-per-day 7.2M
                                    :potential-ccr? false}}
                     {:id 65
                      :description "Shot Peening"
                      :touch-time 72000
                      :work-center {:id "OW008"
                                    :description "SHOT PEENING"
                                    :type :external
                                    :hours-per-day 24M
                                    :potential-ccr? false}}
                     {:id 70
                      :description "Surface Treatments"
                      :touch-time 14400
                      :work-center {:id "OW006"
                                    :description "SURFACE TREATMENTS"
                                    :type :external
                                    :hours-per-day 24M
                                    :potential-ccr? false}}
                     {:id 80
                      :description "Inspection - CMM"
                      :touch-time 60
                      :work-center {:id "IN002"
                                    :description "INSP - MAIN CMM"
                                    :type :internal
                                    :hours-per-day 15.48M
                                    :potential-ccr? false}}]}}}
     "m11"
     {:id {:alternative "1" :revision 1 :type :manufactured}
      :description "Replan (19/3/10)"
      :route-in-use "m7*"
      :components
      {"100105005R01" {:id "100105005R01"
                       :customer-part nil
                       :issue nil
                       :description "AISI 4145 Stainless Steel Bored Billet - 10in O/Dia x 8in I/Dia x 8.250in Length"
                       :type :raw
                       :lead-time 87
                       :best-end-date (t/plus start-date (t/days 87))}
       "100103950R01" {:id "100103950R01"
                       :customer-part nil
                       :issue nil
                       :description "709M40 Steel Bored Billet - 254mm O/Dia x 203mm I/Dia x 210mm Length"
                       :type :raw
                       :lead-time 45
                       :best-end-date (t/plus start-date (t/days 45))}}
      :routes
      {"m7*"
       {:id {:type :manufactured :alternative "*" :revision 7}
        :description "IN HOUSE"
        :best-end-date (t/plus start-date (t/days 155))
        :total-buffer 68.85060292850991
        :total-touch-time 89268
        :ccr {:id "MC041"
              :operation 10
              :total-touch-time 1866
              :pre-ccr-buffer 0
              :post-ccr-buffer 63.01935292850992}
        :operations [{:id 10
                      :description "CNC Lathe"
                      :touch-time 600
                      :work-center {:id "MC041"
                                    :description "MAZAK 3M SLANT TURN"
                                    :type :internal
                                    :hours-per-day 8M
                                    :potential-ccr? true}}
                     {:id 20
                      :description "CNC Lathe"
                      :touch-time 600
                      :work-center {:id "MC041"
                                    :description "MAZAK 3M SLANT TURN"
                                    :type :internal
                                    :hours-per-day 8M
                                    :potential-ccr? true}}
                     {:id 30
                      :description "Fabrication Shop"
                      :touch-time 855
                      :work-center {:id "PR008"
                                    :description "FABRICATION SHOP"
                                    :type :internal
                                    :hours-per-day 8.1M
                                    :potential-ccr? false}}
                     {:id 40
                      :description "CNC Lathe"
                      :touch-time 666
                      :work-center {:id "MC041"
                                    :description "MAZAK 3M SLANT TURN"
                                    :type :internal
                                    :hours-per-day 8M
                                    :potential-ccr? true}}
                     {:id 50
                      :description "Part Marking"
                      :touch-time 48
                      :work-center {:id "PR004"
                                    :description "PART MARKING (IN INSPECTION)"
                                    :type :internal
                                    :hours-per-day 8.1M
                                    :potential-ccr? false}}
                     {:id 60
                      :description "Inspect"
                      :touch-time 39
                      :work-center {:id "IN000"
                                    :description "INSP - MANUAL"
                                    :type :internal
                                    :hours-per-day 7.2M
                                    :potential-ccr? false}}
                     {:id 65
                      :description "Shot Peening"
                      :touch-time 72000
                      :work-center {:id "OW008"
                                    :description "SHOT PEENING"
                                    :type :external
                                    :hours-per-day 24M
                                    :potential-ccr? false}}
                     {:id 70
                      :description "Surface Treatments"
                      :touch-time 14400
                      :work-center {:id "OW006"
                                    :description "SURFACE TREATMENTS"
                                    :type :external
                                    :hours-per-day 24M
                                    :potential-ccr? false}}
                     {:id 80
                      :description "Inspection - CMM"
                      :touch-time 60
                      :work-center {:id "IN002"
                                    :description "INSP - MAIN CMM"
                                    :type :internal
                                    :hours-per-day 15.48M
                                    :potential-ccr? false}}]}}}}}))
