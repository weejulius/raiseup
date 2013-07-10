(ns ^{:doc "the query for read model"
      :added "1.0"}
  raiseup.handler
  (:require [raiseup.ontime.readmodel :as rm]))

(defn find-slot-by-id
  [^Long id]
  (rm/get-readmodel :task-slot id))

(defn find-slots-for-user
  [^Long user-id]
  (rm/query :task-slot (str :user-id "=" user-id)))
