(ns ^{:doc   "the query for read model"
      :added "1.0"}
  ontime.query
  (:require [system :as s]
            [env :as env]
            [cqrs.protocol :refer [Query] :as p]))

(defrecord QuerySlot [^long id ids user-id]
  Query
  (find-by-id [this]
    (fn [readmodel]
      (p/load-entry readmodel :task-slot id)))
  (query [this]
    (fn [readmodel]
      (p/do-query
        readmodel
        :task-slot
        (fn [slot] (= (slot :user-id) user-id))))))
