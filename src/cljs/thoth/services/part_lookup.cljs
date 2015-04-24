(ns thoth.services.part-lookup
  (:require-macros [schema.core :refer [defschema]])
  (:require [schema.core :as s :include-macros true]))

(defschema Part
  {:id s/Str
   :customer-part s/Str
   :customer-issue s/Str
   :description s/Str})

(defschema LookupError
  {:query s/Str
   :error-code (s/maybe s/Any)
   :error s/Str})

(defschema LookupResults
  (s/either
    [(s/one (s/eq :ok) 'ok) {:query s/Str :parts [Part]}]
    [(s/one (s/eq :error) 'error) (s/one LookupError 'lookup-error)]))

(defprotocol PartLookup
  (parts-like
    [pl search-string]
    "Makes an asynchronous request for parts matching `search-string`
    returning a `core.async` channel to which the results will be
    posted. The returned channel will be closed when the request
    completes."))
