(ns ^{:doc "the query for read model"
      :added "1.0"}
  ontime.query
  (:require [system :as s]
            [cqrs.protocol :refer [Query] :as p]))

(defrecord QuerySlot [^long id ids user-id]
  Query
  (find-by-id [this]
    (let [rm (:readmodel s/system)]
      (p/load-entry rm :task-slot id)))
  (query [this]
    (let [rm (:readmodel s/system)]
      (p/do-query
        rm
        :task-slot
        (fn [slot] (= (slot :user-id) user-id))))))
