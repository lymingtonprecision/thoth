(ns thoth.quotes.gantt
  (:require [clojure.zip :as zip]
            [thoth.part-zipper :as pz]
            [cljs-time.core :as t]
            [cljs-time.coerce :as tc]))

(defn best-end-date [x]
  (if-let [bed (:best-end-date x)]
    bed
    (cond
      (:route-in-use x) (best-end-date (get-in x [:routes (:route-in-use x)]))
      (:struct-in-use x) (best-end-date (get-in x [:structs (:struct-in-use x)]))
      :else nil)))

(defn component-end-dates [cp]
  (map
    #(if-let [d (best-end-date %)]
       d)
    (if (map? cp)
      (vals cp)
      cp)))

(defn max-component-end-date [cp]
  (let [ed (component-end-dates cp)]
    (if (seq ed)
      (t/latest ed))))

(defn start-date
  ([x] (start-date x (t/today)))
  ([x default]
   (cond
     (:source x) default
     (:struct-in-use x) (start-date (get-in x [:structs (:struct-in-use x)]))
     (:components x) (or (max-component-end-date (:components x)) default)
     :else default)))

(defn node-is-a-part? [loc]
  (contains? #{:raw :structured} (:type (pz/node-val loc))))

(defn part-loc->gantt-entry [loc]
  (let [part (pz/node-val loc)]
    (assoc (select-keys part [:id :customer-part :issue :description])
           :start-date (start-date part)
           :end-date (best-end-date part)
           :path (pz/path-from-root-to-loc loc))))

(defn remove-unused-structs [part]
  (loop [loc (pz/part-zipper part)]
    (if (zip/end? loc)
      (pz/root-part loc)
      (let [loc (if (:structs (pz/node-val loc))
                  (let [nv (pz/node-val loc)
                        siu (:struct-in-use nv)
                        s (get-in nv [:structs siu])]
                    (pz/edit-val loc assoc :structs {siu s}))
                  loc)]
        (recur (zip/next loc))))))

(defn elements
  "Returns a collection of the parts present in the quote that should be
  drawn on the gantt chart. Each part includes its:

  * `:id`
  * `:customer-part`
  * `:issue`
  * `:description`
  * `:start-date`
  * `:end-date`"
  [q]
  (loop [ge []
         loc (pz/part-zipper (remove-unused-structs q))]
    (if (zip/end? loc)
      ge
      (let [ge (if (node-is-a-part? loc)
                 (conj ge (part-loc->gantt-entry loc))
                 ge)]
        (recur ge (zip/next loc))))))
