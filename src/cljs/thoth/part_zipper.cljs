(ns thoth.part-zipper
  (:require [clojure.zip :as zip]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Constants

(def branching-keys
  "Which subkeys denote a branch in a part structure? These subkeys."
  [:structs :components :routes])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utility fns

(defn branch?
  "Returns truthy if `x` contains a truthy value for one of the
  `branching-keys`."
  [x]
  (some #(get x %) branching-keys))

(defn branches
  "Returns a sequence of the `branching-keys` present in `x`, each
  branch is returned as a single element map of `{branching-key values}`.
  Note: if `x` is a purchased structure a phantom `:routes` branch is
  also returned to keep the traversal semantics the same as manufactured
  structures."
  [x]
  (let [b (reduce
           (fn [r k] (if-let [v (get x k)] (conj r {k v}) r))
           []
           branching-keys)]
    (if (and (seq b) (= (-> x :id :type) :purchased))
      (conj b {:routes {}})
      b)))

(defn branch-nodes
  "Returns the branch nodes that the zipper should traverse from `x`.
  When `x` has a single branch this is the sequence of items within that
  branch, each expressed as a single element map of `{key value}`.
  When `x` has multiple branches this is the sequence of those branches."
  [x]
  (let [b (branches x)
        n (count b)]
    (if (= n 1)
      (let [[_ nodes] (-> b first seq first)]
        (reduce
         (fn [r [k v]] (conj r {k v}))
         []
         nodes))
      b)))

(defn use-record-or-value
  "Returns a fn that calls `f` with it's arguments after potentially
  substituting the first argument with the value of it's first entry
  if it doesn't satisfy `branch?`
      (let [f (use-record-or-value #(clojure.pprint/pprint %))]
        (f {:structs {}})
        (f {1 {:components {} :routes {}}))
      ; prints:
      ; {:structs {}}
      ; {:components {} :routes {}}
  "
  [f]
  (fn [x & args]
    (if (branch? x)
      (apply f x args)
      (apply f (-> x vals first) args))))

(defn conservative-merge
  "Like merge but only merges keys that either exist in the left side
  map or aren't empty collections in the right side map."
  [& maps]
  (when (some identity maps)
    (reduce
     (fn [r m]
       (reduce
        (fn [r kv] (apply assoc r kv))
        r
        (filter
         (fn [[k v]] (or (contains? r k)
                         (not (coll? v))
                         (not-empty v)))
         m)))
     (first maps)
     (rest maps))))

(defn take-until
  "Like `take-while` but includes the item that matches `pred`."
  [pred coll]
  (let [[t r] (split-with (complement pred) coll)]
    (if-let [x (first r)]
      (conj (vec t) x)
      (vec t))))

(defn ^:private -node-key [loc]
  (-> loc zip/node keys first))

(defn invalid-part-zipper-entry
  "Returns a message denoting the unexpected nature of `x` when
  encountered as a node within a part zipper."
  [x]
  (str "expected single element map, don't know how to zip a part entry like: "
       x))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn part-zipper
  "Returns a zipper for traversing a part structure."
  [part]
  (zip/zipper
   (use-record-or-value branch?)
   (use-record-or-value branch-nodes)
   (fn [node children]
     (assert (= 1 (count node)) (invalid-part-zipper-entry node))
     (let [k (if (:structs (first (vals node)))
               (conj (vec (keys node)) :structs)
               (keys node))
           v (if (some #(= (last k) %) branching-keys)
               (apply merge children)
               (apply conservative-merge (first (vals node)) children))]
       (assoc-in node k v)))
   {::part part}))

(defn root-part
  "Returns the part record at the root of a part-zipper.
  (Note: you should use this rather than `clojure.zipper/root` because
  we wrap the part before passing it to the zipper and this will undo
  that extra level of wrapping.)"
  [z]
  (::part (zip/root z)))

(defn node-key
  "Returns the key of the node at `loc`."
  [loc]
  (let [k (-node-key loc)]
    (if (not= ::part k) k)))

(defn node-val
  "Returns the value of the node at `loc`."
  [loc]
  (-> loc zip/node vals first))

(defn edit-val
  "Replaces the node at `loc` with the value of `(f node-value args)`,
  keeping its key."
  [loc f & args]
  (let [k (-node-key loc)
        v (apply f (node-val loc) args)]
    (zip/replace loc {k v})))

(defn path-from-loc-to-root
  [loc]
  (let [path-to-item (if-let [p (zip/path loc)]
                       (->> (map #(-> % vals first :id) p)
                            (remove nil?)
                            reverse)
                       [])
        full-path (conj path-to-item (-> loc zip/node vals first :id))]
    full-path))

(defn path-from-root-to-loc
  [loc]
  (-> loc path-from-loc-to-root reverse vec))

(defn path-from-loc-to-part
  "Returns the path from the zipper location `loc` to the part to which
  it belongs.
  If `loc` is a part then a sequence containing just the part number is
  returned.
  If `loc` is a routing or structure then a sequence of IDs leading back
  to the part to which it belongs is returned:
     [route-id struct-id part-id]
     ; or
     [struct-id part-id]
  "
  [loc]
  (let [full-path (path-from-loc-to-root loc)]
    (vec (take-until string? full-path))))

(defn path-from-part-to-loc
  "A convenience for reversing the results of `path-from-loc-to-part`."
  [loc]
  (-> loc path-from-loc-to-part reverse vec))
