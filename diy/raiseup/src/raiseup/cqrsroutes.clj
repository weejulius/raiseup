(ns raiseup.cqrsroutes
  (:require [raiseup.ontime.commands :refer :all]
            [raiseup.ontime.events :refer :all]))

(def commandroutes
  {:create-task-slot create-task-slot} )

(def eventroutes
  {:task-slot-created [[task-slot-created]]})
