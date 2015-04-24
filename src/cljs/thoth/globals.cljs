(ns thoth.globals)

(def app-state
  (atom {:quotes {}
         :active-quote nil
         :page-fn nil}))
