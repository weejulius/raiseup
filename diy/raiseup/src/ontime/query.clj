(ns ^{:doc "the query for read model"
      :added "1.0"}
  ontime.query
  (:require [ontime :refer [entries]]))

(defn find-slot-by-id
  [^Long id]
  (.load-entry entries :task-slot id))

(defn find-slots-for-user
  [^Long user-id]
  (.query entries :task-slot (fn [slot] (= (slot :user-id) user-id))))
