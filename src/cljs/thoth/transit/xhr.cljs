(ns thoth.transit.xhr
  (:require-macros [schema.core :refer [defschema]])
  (:require [cljs-time.format :as tf]
            [cognitect.transit :as tr]
            [com.cognitect.transit.types :as trt]
            [schema.core :as s :include-macros true]
            [goog.events :as events])
  (:import [goog.net XhrIo]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema

(def http-methods
  "The HTTP request codes we support Transit payloads in."
  {:get "GET"
   :post "POST"})

(defschema http-method
  (apply s/enum (keys http-methods)))

(defschema TransitXhrError
  {:uri s/Str
   :status-code s/Int
   :status s/Str
   :error-code (s/maybe s/Int)
   :error s/Str
   (s/optional-key :response) s/Any})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Encoding/decoding

(def date-formatter (:date tf/formatters))

(def write-handlers
  {goog.date.Date
   (tr/write-handler
     (fn [d] "Date")
     (fn [d] (tf/unparse-local-date date-formatter d)))})

(def read-handlers
  {"f"
   (tr/read-handler
     (fn [v] (trt/floatValue v)))
   "Date"
   (tr/read-handler
     (fn [d] (tf/parse-local-date d)))})

(def transit-json-reader
  "A transit+json reader, using our custom handlers."
  (tr/reader :json {:handlers read-handlers}))

(def transit-json-writer
  "A transit+json writer, using our custom handlers."
  (tr/writer :json {:handlers write-handlers}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

(s/defn xhr-err :- TransitXhrError
  "Returns a map of the error details from an `XhrIo` object."
  [xhr]
  {:uri (.getLastUri xhr)
   :status-code (.getStatus xhr)
   :status (.getStatusText xhr)
   :error-code (.getLastErrorCode xhr)
   :error (.getLastError xhr)})

(defn log-failed-request
  "Prints the error details to the browser console."
  [err]
  (.log js/console (str "transit request failed: " (pr-str err))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(s/defn transit-xhr
  "Makes an asynchronous HTTP request, encoding/decoding the data
  sent/received as `transit+json`.

  Calls `success-fn` with the returned (decoded) data or `error-fn` with
  the error details if there is any error making the request.

  Will log the error details in the browser console if no `error-fn` is
  provided."
  [{:keys [method url data success-fn error-fn] :- {method http-method}
    :or {error-fn log-failed-request}}]
  (let [xhr (XhrIo.)]
    (events/listen
      xhr goog.net.EventType.ERROR
      #(error-fn (xhr-err (.-target %))))
    (events/listen
      xhr goog.net.EventType.SUCCESS
      (fn [e]
        (let [xhr (.-target e)
              resp (.getResponseText xhr)]
          (try
            (success-fn (tr/read transit-json-reader resp))
            (catch js/Object e
              (error-fn
                (assoc (xhr-err xhr)
                       :error-code nil
                       :error "transit processing failed"
                       :response resp)))))))
    (. xhr
       (send url
             (http-methods method)
             (when data (tr/write transit-json-writer data))
             #js {"Content-Type" "application/transit+json"}))))
