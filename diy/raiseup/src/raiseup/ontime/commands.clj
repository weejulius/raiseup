(ns ^{:doc "the commands"
      :added "1.0"}
  raiseup.ontime.commands
  (:require [cqrs.protocol :as cqrs]
            [bouncer [core :as b] [validators :as v]])
  (:use [cqrs.protocol :only [handle-command]]))

(defrecord CreateTaskSlot [ar ar-id user-id description start-time estimation]
  cqrs/Validatable
  (validate [cmd]
    (b/validate cmd :description v/required)))

(defrecord DeleteTaskSlot [ar ar-id user-id])
(defrecord StartTaskSlot [ar ar-id start-time]
  cqrs/Validatable
  (validate [cmd]
    (b/validate cmd
                :ar-id [v/required]
                :start-time v/required)))

(defmethod handle-command
  DeleteTaskSlot
  [cmd]
  (cqrs/gen-event
   :task-slot-deleted
   cmd :user-id))

(defmethod handle-command
  StartTaskSlot
  [cmd]
  (cqrs/gen-event
   :task-slot-started
   cmd :start-time))

(defmethod handle-command
  CreateTaskSlot
  [cmd]
  (cqrs/gen-event
   :task-slot-created
   cmd :description :start-time :estimation :user-id))
