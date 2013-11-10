(ns ^{:doc "the query for read model"
      :added "1.0"}
  ontime.query
  (:require [system :refer [entries]]
            [cqrs.protocol :refer [Query]]))

(defrecord QuerySlot [^Long id ids ^Long user-id]
  Query
  (find-by-id [this] (.load-entry entries :task-slot id))
  (query [this]
    (.do-query entries :task-slot (fn [slot] (= (slot :user-id) user-id)))))
