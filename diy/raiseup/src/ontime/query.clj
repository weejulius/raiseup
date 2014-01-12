(ns ^{:doc "the query for read model"
      :added "1.0"}
  ontime.query
  (:require [system :as s]
            [cqrs.protocol :refer [Query]])
  (:import (cqrs.protocol ReadModel)))

(defrecord QuerySlot [^Long id ids user-id]
  Query
  (find-by-id [this]
    (let [^ReadModel rm (:readmodel s/system)]
      (.load-entry rm :task-slot id)))
  (query [this]
    (let [^ReadModel rm (:readmodel s/system)]
      (.do-query
        rm
        :task-slot
        (fn [slot] (= (slot :user-id) user-id))))))
