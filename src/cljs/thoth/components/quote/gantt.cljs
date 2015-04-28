(ns thoth.components.quote.gantt
  (:require [cljsjs.d3]
            [cljs-time.core :as t]
            [cljs-time.coerce :as tc]
            [thoth.quotes.gantt :as qg]))

(defn clj-entry->js [e]
  (clj->js
    (assoc e
           :start-date (tc/to-date (:start-date e))
           :end-date (tc/to-date (:end-date e)))))

(defn draw-gantt [el selector data]
  (let [width 960
        height 500
        entries (qg/elements data)
        dr [(t/earliest (map :start-date entries))
            (t/latest (map :end-date entries))]
        g (-> (js/d3.select el selector)
              (.append "svg")
              (.attr "width" width)
              (.attr "height" height))
        ts (-> (js.d3.time.scale)
               (.domain (clj->js (map tc/to-date dr)))
               (.range #js [0 width])
               (.clamp true))
        rects (-> (.append g "g")
                  (.selectAll "rect")
                  (.data (clj->js (map clj-entry->js entries)))
                  (.enter))
        bars (-> (.append rects "rect")
                 (.attr "rx" 3)
                 (.attr "ry" 3)
                 (.attr "x" #(ts (aget % "start-date")))
                 (.attr "y" (fn [_ i] (* i 30)))
                 (.attr "width" #(- (ts (aget % "end-date"))
                                    (ts (aget % "start-date"))))
                 (.attr "height" 29)
                 (.attr "stroke" "black")
                 (.attr "fill" "none"))]
    g))
