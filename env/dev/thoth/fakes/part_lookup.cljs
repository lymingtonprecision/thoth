(ns thoth.fakes.part-lookup
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [chan close! <! put! timeout]]
            [schema.core :as s :include-macros true]
            [thoth.services.part-lookup :refer [Part PartLookup]]))

(s/defn fake-part-lookup [& parts :- [Part]]
  (reify
    PartLookup
    (parts-like [_ q]
      (let [results-chan (chan)]
        (if (re-find #"(?i)error" q)
          (do
            (put! results-chan
                  [:error {:query q
                           :error-code 500
                           :error "part lookup service returned error"}])
            (close! results-chan))
          (let [search-p (re-pattern (str "(?i)"
                                          (-> q
                                              (str/replace #"[.?+^$\[\](){}|-]"
                                                           "\\$1")
                                              (str/replace "*" ".*")
                                              (str/replace "_" ".")
                                              (str/replace #" +" ".*"))))
                matching-parts (filter (fn [p]
                                         (some #(re-find search-p %) (vals p)))
                                       parts)]
            (go
              (<! (timeout (* (rand-nth (range 5 30)) 100)))
              (put! results-chan [:ok {:query q :parts matching-parts}])
              (close! results-chan))))
        results-chan))))
