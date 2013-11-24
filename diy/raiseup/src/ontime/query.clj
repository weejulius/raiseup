(ns ^{:doc "the query for read model"
      :added "1.0"}
  ontime.query
  (:require [system :as s]
            [cqrs.protocol :refer [Query]]))

(defrecord QuerySlot [^Long id ids user-id]
  Query
  (find-by-id [this] (.load-entry (:readmodel s/system) :task-slot id))
  (query [this]
    (.do-query
     (:readmodel s/system)
     :task-slot
     (fn [slot] (= (slot :user-id) user-id)))))
