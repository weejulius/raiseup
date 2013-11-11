(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [system :refer [entries]]
            [cqrs.protocol :refer [Query]]))

(defrecord QueryNote [id author]
  Query
  (find-by-id [this]
    (.load-entry entries :note id))
  (query [this]
    (.do-query
     entries :note
     (fn [note]
       (if-not (nil? author)
         (= (:author note) author)
         true)))))
