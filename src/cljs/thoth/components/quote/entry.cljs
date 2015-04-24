(ns thoth.components.quote.entry
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [thoth.transit.chan :as trc]))

(defcomponent quote-entry [_ owner]
  (init-state
    [_]
    {:waiting-for nil
     :part nil})
  (will-mount
    [this]
    (trc/on-transit-result
      owner this
      :ok #(om/set-state! owner {:waiting-for nil :part %})
      :error #(.log js/console (str "error requesting part " (pr-str %)))))
  (did-mount
    [this]
    (let [part "100105001R03"
          url (str "http://localhost:61627/parts/" part)]
      (trc/make-transit-request owner this :get url)
      (om/set-state! owner :waiting-for part)))
  (render-state
    [_ {:keys [part]}]
    (dom/div
      (dom/h3 "Quote Entry")
      (if part
        (dom/code (pr-str part))))))
