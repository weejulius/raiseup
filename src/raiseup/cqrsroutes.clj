(ns raiseup.cqrsroutes
  (:require [raiseup.ontime.commands :as commands]
            [raiseup.ontime.model :refer :all]
            [raiseup.ontime.readmodel :as readmodel]))

(def commandroutes
  {:create-task-slot commands/create-task-slot
   :delete-task-slot commands/delete-task-slot} )

(def eventroutes
  {:task-slot-created [[task-slot-created]
                       [readmodel/task-slot-created]]
   :task-slot-deleted [[task-slot-deleted]
                       [readmodel/task-slot-deleted]]})
