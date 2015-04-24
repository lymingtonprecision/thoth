(ns thoth.services.quotes
  (:require-macros [schema.core :refer [defschema]])
  (:require [schema.core :as s :include-macros true]))

(defschema QuoteRequestError
  {:request-data s/Any
   :error-code (s/maybe s/Any)
   :error s/Str})

(defschema QuoteResponse
  (s/either
    [(s/one (s/eq :ok) 'ok) s/Any]
    [(s/one (s/eq :error) 'error) (s/one QuoteRequestError 'req-error)]))

(defprotocol QuoteService
  (request-quote-for-part
    [qs part-no]
    "Makes an asynchronous request for a new quote of the specified part
    returning a `core.async` channel to which the results will be posted.")
  (request-updated-end-dates
    [qs quote-data]
    "Makes an asynchronous request for an updated version of the
    supplied quote returning a `core.async` channel to which the results
    will be posted."))
