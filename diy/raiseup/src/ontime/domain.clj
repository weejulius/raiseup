(ns ontime.domain
  (:require [cqrs.core :as cqrs]))

(defn create-task
  "create a task"
  [{:keys [ar ar-id description user-id estimation]}]
  {:event :task-slot-created
   :ar ar
   :ar-id ar-id
   :description description
   :user-id user-id
   :estimation estimation})

(defn start-task
  [{:keys [ar ar-id start-time]}]
  {:event :task-slot-started
   :ar ar
   :ar-id ar-id
   :start-time start-time})

(defn delete-task
  [{:keys [ar ar-id]}]
  {:event :task-slot-deleted
   :ar ar
   :ar-id ar-id})
