(ns thoth.fakes.quote-service
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put! timeout]]
            [cljs-time.core :as t]
            [thoth.services.quotes :refer [QuoteService]]
            [thoth.fakes.quotes.thread-protector :refer [thread-protector-quote]]))

(defn get-quote-for [quotes {:keys [id] :as req}]
  (if-let [q (get quotes id)]
    [:ok (q (t/plus (t/today) (t/days (rand-int 20))))]
    [:error {:request-data req :error-code 500 :error "quote service returned error"}]))

(defn results-chan [quotes req]
  (let [c (chan)
        r (get-quote-for quotes req)]
    (go
      (<! (timeout (* (rand-nth (range 1 5)) 1000)))
      (put! c r)
      (close! c))
    c))

(defn fake-quote-service []
  (let [quotes {"100105001R03" thread-protector-quote}]
    (reify
      QuoteService
      (request-quote-for-part [_ part-no]
        (results-chan quotes {:id part-no}))
      (request-updated-end-dates [_ q]
        (results-chan quotes q)))))
