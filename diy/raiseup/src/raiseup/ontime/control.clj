(ns raiseup.ontime.control
  (:require [raiseup.commandbus :as cb]
            [raiseup.cqrsroutes :refer :all]))

(defn- send
  "send command to bus"
  [command]
  (cb/->send command commandroutes eventroutes))

(defn create-task-slot-action
  "create an task slot"
  [[description start-time estimation]]
  (send {:command :create-task-slot
         :ar :task-slot
         :description description
         :start-time start-time
         :estimation estimation}))



