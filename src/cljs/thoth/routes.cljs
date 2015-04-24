(ns thoth.routes
  (:require [bidi.bidi :as b]))

(def routes (atom ["/" {}]))

(defn defroute [name path handler-fn]
  (swap! routes (fn [r] (assoc-in r [1 path] (b/->TaggedMatch name handler-fn)))))

(defn match-route [& path]
  (apply b/match-route @routes path))

(defn path-for [& path]
  (apply b/path-for @routes path))
