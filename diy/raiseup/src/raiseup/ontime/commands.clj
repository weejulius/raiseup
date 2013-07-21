(ns ^{:doc "the command handlers"
      :added "1.0"}
  raiseup.ontime.commands
  (:require [cqrs.protocol :as cqrs]))

(defrecord CreateTaskSlot [ar ar-id user-id description start-time estimation]
  cqrs/Command
  (handle-command [this]
    {:event :task-slot-created
     :ar ar
     :ar-id ar-id
     :description description
     :start-time start-time
     :estimation estimation
     :user-id user-id
     :ect (java.util.Date.)})
  )

(defrecord DeleteTaskSlot [ar ar-id user-id]
  cqrs/Command
  (handle-command [this]
    {:event :task-slot-deleted
     :ar ar
     :ar-id ar-id
     :user-id user-id
     :ect (java.util.Date.)}))

(defrecord StartTaskSlot [ar ar-id start-time]
  cqrs/Command
  (handle-command [this]
    {:event :task-slot-started
     :ar ar
     :ar-id ar-id
     :start-time start-time
     :etc (java.util.Date.)}))

