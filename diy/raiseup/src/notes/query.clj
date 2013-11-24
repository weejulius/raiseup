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
     (fn [note]
       (if-not (nil? author)
         (= (:author note) author)
         true))
     (fn [m]
       (or (nil? size) (>= size (count m)))))))
