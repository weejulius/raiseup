(ns raiseup.ontime.model-test
  (:use midje.sweet
        raiseup.ontime.model))

(facts "task is started after attempt to work on the task"
      (let [new-task (create-task 1 "task1" "owner" 30 nil)]
        ((get-in (attempt new-task 30 nil) [:attempts 0 :status]) => :started)))
