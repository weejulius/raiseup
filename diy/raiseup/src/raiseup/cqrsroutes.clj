(ns raiseup.cqrsroutes
  (:require [raiseup.ontime.commands :refer :all]
            [raiseup.ontime.model :refer :all]
            [raiseup.ontime.views :as view]))

(def commandroutes
  {:create-task-slot create-task-slot} )

(def eventroutes
  {:task-slot-created [[task-slot-created]
                       [view/task-slot-created]]})
