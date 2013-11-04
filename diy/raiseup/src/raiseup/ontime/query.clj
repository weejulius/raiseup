(ns ^{:doc "the query for read model"
      :added "1.0"}
  raiseup.ontime.query
  (:require [ontime :refer [models]]))

(defn find-slot-by-id
  [^Long id]
  (.load-entry models :task-slot id))

(defn find-slots-for-user
  [^Long user-id]
  (.query models :task-slot (fn [slot] (= (slot :user-id) user-id))))
