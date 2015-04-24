(ns thoth.fakes.quotes.lock-plate
  (:require [cljs-time.core :as t]))

(defn tp [sd i]
  (t/plus sd (t/days i)))

(defn lock-plate-quote
  ([] (lock-plate-quote (t/today)))
  ([start-date]
   {:id "100101042R01"
    :type :structured
    :best-end-date nil
    :struct-in-use 1
    :structs
    {1 {:id {:type :manufactured :revision 1 :alternative "*"}
        :route-in-use 1
        :routes
        {1 {:id {:type :manufactured :revision 1 :alternative "*"}
            :best-end-date (tp start-date (+ 4 10 2 22))
            :ccr nil
            :total-touch-time (* 20 60)
            :total-buffer 3.75
            :operations
            [{:id 10
              :touch-time (* 15 60)
              :work-center {:id "PR002"
                            :type :internal
                            :hours-per-day 8
                            :potential-ccr? false}}
             {:id 20
              :touch-time (* 5 60)
              :work-center {:id "IN001"
                            :type :internal
                            :hours-per-day 8
                            :potential-ccr? false}}]}}
        :components
        ;; the critical path component
        ;; at least 3 levels and 6 parts
        {1 {:id "100182830R01"
            :type :structured
            :best-end-date nil
            :struct-in-use 1
            :structs
            {1 {:id {:type :purchased :revision 1 :alternative "*"}
                :best-end-date (tp start-date (+ 10 2 22))
                :lead-time 10
                :components
                {1 {:id "100117839R01"
                    :type :structured
                    :best-end-date nil
                    :struct-in-use 1
                    :structs
                    {1 {:id {:type :manufactured :revision 1 :alternative "*"}
                        :route-in-use 1
                        :routes
                        {1 {:id {:type :manufactured :revision 1 :alternative "*"}
                            :best-end-date (tp start-date (+ 2 22))
                            :ccr {:id "MC032"
                                  :operation 10
                                  :total-touch-time (* 2 60)
                                  :pre-ccr-buffer 0
                                  :post-ccr-buffer 1.5}
                            :total-touch-time (* 10 60)
                            :total-buffer 1.875
                            :operations
                            [{:id 10
                              :touch-time (* 2 60)
                              :work-center {:id "MC032"
                                            :type :internal
                                            :hours-per-day 8
                                            :potential-ccr? true}}
                             {:id 20
                              :touch-time (* 5 60)
                              :work-center {:id "PR001"
                                            :type :internal
                                            :hours-per-day 8
                                            :potential-ccr? false}}
                             {:id 30
                              :touch-time (* 3 60)
                              :work-center {:id "IN000"
                                            :type :internal
                                            :hours-per-day 8
                                            :potential-ccr? false}}]}}
                        :components
                        {1 {:id "100132479R01"
                            :type :raw
                            :best-end-date (tp start-date 22)
                            :lead-time 22}
                         2 {:id "100129852R01"
                            :type :raw
                            :best-end-date (tp start-date 6)
                            :lead-time 6}}}}}
                 2 {:id "100184752R01"
                    :type :structured
                    :best-end-date nil
                    :struct-in-use 1
                    :structs
                    {1 {:id {:type :purchased :revision 1 :alternative "*"}
                        :best-end-date (tp start-date (+ 5 5 10))
                        :lead-time 5
                        :components
                        {1 {:id "100171944R01"
                            :type :raw
                            :best-end-date (tp start-date 7)
                            :lead-time 7}
                         2 {:id "100157294R01"
                            :type :raw
                            :best-end-date (tp start-date 9)
                            :lead-time 9}
                         3 {:id "100157601R01"
                            :type :structured
                            :best-end-date nil
                            :struct-in-use 1
                            :structs
                            {1 {:id {:type :purchased :revision 1 :alternative "*"}
                                :best-end-date (tp start-date (+ 5 10))
                                :lead-time 5
                                :components
                                {1 {:id "100134202E01"
                                    :type :raw
                                    :best-end-date (tp start-date 10)
                                    :lead-time 10}}}}}}}}}}}}}
         ;; a simple non-critical component
         ;; 2-3 levels, 2-4 parts
         2 {:id "100142598R01"
            :type :structured
            :best-end-date nil
            :struct-in-use 1
            :structs
            {1 {:id {:type :purchased :revision 1 :alternative "*"}
                :best-end-date (tp start-date (+ 14 4 4))
                :lead-time 14
                :components
                {1 {:id "100192812R01"
                    :type :raw
                    :best-end-date (tp start-date 7)
                    :lead-time 7}
                 2 {:id "100114258R01"
                    :type :raw
                    :best-end-date (tp start-date 4)
                    :lead-time 4}
                 3 {:id "100139873R01"
                    :type :structured
                    :best-end-date nil
                    :struct-in-use 1
                    :structs
                    {1 {:id {:type :purchased :revision 1 :alternative "*"}
                        :best-end-date (tp start-date (+ 4 4))
                        :lead-time 4
                        :components
                        {1 {:id "100138419R01"
                            :type :raw
                            :best-end-date (tp start-date 3)
                            :lead-time 3}
                         2 {:id "100113875R01"
                            :type :raw
                            :best-end-date (tp start-date 4)
                            :lead-time 4}}}}}}}}}}}}}))
