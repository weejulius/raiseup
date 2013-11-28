(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [system :as s]
            [cqrs.protocol :refer [Query]]))


(defrecord QueryNote [id author page size]
  Query
  (find-by-id [this]
    (.load-entry
     (:readmodel s/system) :note id))
  (query [this]
    (.do-query
     (:readmodel s/system) :note
     :query (-> {}
                (if-not (nil? author) (assoc :term {:author author}))
                (if-not (nil? ))))))
