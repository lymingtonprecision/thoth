(ns thoth.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [chan sliding-buffer pub sub map> <! put!]]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]

            [thoth.globals :as globals]
            [thoth.navigation :as navigation]
            [thoth.quotes.creation :as quote-creation]

            [thoth.components.app :refer [quote-app]]))

(enable-console-print!)

(defn build-root [app-state services]
  (let [msg-chan (chan)
        msg-pub (pub msg-chan first)]
    (om/root
      quote-app
      globals/app-state
      {:shared (merge services
                      {:message-chan msg-chan
                       :message-pub msg-pub})
       :target (.getElementById js/document "app")})))

(defn main [services]
  (let [root (build-root globals/app-state services)]
    (navigation/subscribe-to-messages! root)
    (quote-creation/subscribe-to-messages! root)
    root))
