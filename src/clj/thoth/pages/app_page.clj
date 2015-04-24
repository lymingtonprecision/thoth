(ns thoth.pages.app-page
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :refer :all]))

(defn s->t [s] (java.io.StringReader. s))

(def app-page-html
  "<!DOCTYPE html>
<html>
  <head>
    <title>Thoth</title>
  </head>
  <body>
    <div id='app'></div>
    <script type='text/javascript' src='/js/app.js'></script>
  </body>
</html>")

(def inject-devmode-html
  (comp
    (set-attr :class "dev-mode")
    (prepend (html [:script {:type "text/javascript"
                             :src "/js/out/goog/base.js"}]))
    (append  (html [:script {:type "text/javascript"}
                    "goog.require('thoth.main')"]))))

(deftemplate app-page (s->t app-page-html) [dev-mode?]
  [:body] (if dev-mode? inject-devmode-html identity))
