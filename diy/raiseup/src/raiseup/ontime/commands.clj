(ns ^{:doc "the command handlers"
      :added "1.0"}
  raiseup.ontime.commands
  (:require [cqrs.protocol :as cqrs]))

(defrecord CreateTaskSlot [ar ar-id user-id description start-time estimation]
  cqrs/Command
  (handle-command [this]
    (cqrs/gen-event :task-slot-created ar ar-id
                    {:description description
                     :start-time start-time
                     :estimation estimation
                     :user-id user-id})))

(defrecord DeleteTaskSlot [ar ar-id user-id]
  cqrs/Command
  (handle-command [this]
    (cqrs/gen-event  :task-slot-deleted ar ar-id
                     {:user-id user-id})))

(defrecord StartTaskSlot [ar ar-id start-time]
  cqrs/Command
  (handle-command [this]
    (cqrs/gen-event  :task-slot-started
                     ar
                     ar-id
                     {:start-time start-time})))

