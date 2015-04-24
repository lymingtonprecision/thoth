(ns thoth.transit.chan
  "Provides `fn`s for processing asynchronous HTTP requests for Transit
  encoded data within Om components with as little ceremony as possible.

  ```clojure
  ; in your app root, share the channel map:
  (om/root
    my-app
    app-state
    {:shared {:transit-xhr (make-transit-xhr-chans)}})

  ; in your components, make requests and update state from received
  ; results:
  (defn my-component [_ owner]
    (reify
      ; set some initial state, could be in the app state rather than
      ; component local
      om/InitState
      (init-state [_]
        {:data nil :error nil})

      ; setup the response handling in `will-mount`
      om/IWillMount
      (will-mount [this]
        (on-transit-result
          owner this
          :ok #(om/set-state! owner {:data % :error nil})
          :error #(om/set-state! owner {:data nil :error %})))

      ; make initial requests in `did-mount` (and/or make requests as
      ; the result of user interaction)
      om/IDidMount
      (did-mount [this]
        (make-transit-request owner this :get \"http://some/service\"))

      ; render your component as normal
      om/IRenderState
      (render-state [_ {:keys [data error]}]
        (cond
          data (dom/code nil (pr-str data))
          error (dom/p nil \"Error making request\")
          :else (dom/p nil \"Waiting ... waiting ... waiting\")))))
  ```"
  (:require-macros [cljs.core.async.macros :refer [go-loop]]
                   [schema.core :refer [defschema]])
  (:require [cljs.core.async :refer [chan pub sub put! <!]]
            [schema.core :as s :include-macros true]
            [om.core :as om]
            [thoth.transit.xhr :as xhr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema

(defschema TransitXhrRequest
  {:requestor s/Any
   :method xhr/http-method
   :url s/Str
   :data (s/maybe s/Any)})

(defschema TransitResponseType
  (s/enum :ok :error))

(defschema TransitXhrResponse
  (s/either
    [(s/one (s/eq :error) 'error)
     (s/one {:requestor s/Any :data xhr/TransitXhrError} 'error-response)]
    [(s/one (s/eq :ok) 'ok)
     (s/one {:requestor s/Any :data s/Any} 'ok-response)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Request handling fns

(s/defn put-response!
  "`put!`s a `TransitXhrResponse` of type `resp-type` onto channel `c`
  using the appropriate details from the request, `req`, and response
  data, `d`."
  [c, resp-type :- TransitResponseType, req :- TransitXhrRequest, d]
  (put! c [resp-type {:requestor (:requestor req) :data d}]))

(defn process-loop
  "Creates a `go` block that takes `TransitXhrRequest`s from `req-chan`
  and puts the responses on to `res-chan`.

  Note that the responses may not arrive in the same order in which the
  requests are posted. Each request is executed asynchronously and the
  results posted as they become available."
  [req-chan res-chan]
  (go-loop
    []
    (if-let [r (<! req-chan)]
      (xhr/transit-xhr
        (assoc (dissoc r :requestor)
               :success-fn (partial put-response! res-chan :ok r)
               :error-fn (partial put-response! res-chan :error r))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Response handling fns

(s/defn process-requestor-results-of-type
  "Creates a `go` block that takes `TransitXhrResponse`s for requests
  made by `requestor` of type `res-type` from the result channel of the
  `:transit-xhr` processor shared with `owner` and calls `res-fn` with
  the response data as its only argument."
  [owner, requestor, res-type :- TransitResponseType, res-fn]
  (let [requestor-chan (:requestor-chan (om/get-shared owner :transit-xhr))
        res-chan (chan)
        res-sub (sub requestor-chan [res-type requestor] res-chan)]
    (go-loop
      []
      (if-let [r (<! res-chan)]
        (res-fn (-> r second :data))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn make-transit-xhr-chans
  "Returns a map containing request and response channels for making
  and processing the results of asynchronous HTTP requests for transit
  encoded data, with a `go` block for shuttling things between the two.

  The returned map contains:

  * `:req-chan` a channel onto which `TransitXhrRequest`s should be
    posted.
  * `:res-chan` a channel onto which the `TransitXhrResponse`s of all
    posted requests will be put.
  * `:requestor-chan` a `pub`lication of the results channel splitting
    the results into topics of `[response-type requestor]`.
  * `:process` the `go` block taking requests from `:req-chan` and
    posting the results to `:res-chan`.

  This map should be made available under the `:transit-xhr` key of the
  Om shared data map so that components can call the other `fn`s in this
  namespace with their `owner` as the first argument.

  It is not intended that you interact with any of these elements
  directly."
  []
  (let [req-chan (chan)
        res-chan (chan)
        req-res-chan (pub res-chan (fn [r] [(first r) (-> r second :requestor)]))
        req-process (process-loop req-chan res-chan)]
    {:req-chan req-chan
     :res-chan res-chan
     :requestor-chan req-res-chan
     :process req-process}))

(s/defn make-transit-request
  "`put!`s a new `TransitXhrRequest` to the `:transit-xhr` request
  channel shared with `owner`, with the `requestor`, `method`, `url`,
  and optional `data` forming the details of the request."
  ([owner requestor method url]
   (make-transit-request owner requestor method url nil))
  ([owner requestor method :- xhr/http-method url data]
   (let [req-chan (:req-chan (om/get-shared owner :transit-xhr))]
     (put! req-chan {:method method
                     :url url
                     :data data
                     :requestor requestor}))))

(defn on-transit-success
  "Creates a `go` block for processing successful requests made by
  `requestor`, calling `res-fn` with the data from each request as
  it arrives."
  [owner requestor res-fn]
  (process-requestor-results-of-type owner requestor :ok res-fn))

(defn on-transit-error
  "Creates a `go` block for processing failed requests made by
  `requestor`, calling `res-fn` with the error details from each
  request as they are generated."
  [owner requestor res-fn]
  (process-requestor-results-of-type owner requestor :error res-fn))

(defn on-transit-result
  "A convenience over making separate `on-transit-success` and
  `on-transit-error` calls.

      (on-transit-success owner requestor #(.log js/console \"success\"))
      (on-transit-error owner requestor #(.log js/console \"error\"))

  Becomes:

      (on-transit-result
        owner requestor
        :ok #(.log js/console \"success\")
        :error #(.log js/console \"error\"))

  Either key may be omitted to only create a processor for a specific type
  of response."
  [owner requestor & {:keys [ok error] :as handlers}]
  (let [assoc-if (fn [m t k v] (if t (assoc m k v) m))]
    (-> {}
        (assoc-if ok :ok (on-transit-success owner requestor ok))
        (assoc-if error :error (on-transit-error owner requestor error)))))
