(ns raiseup.t-cqrsroutes
  (:use [midje.sweet]
        [raiseup.cqrsroutes]))

(fact "get event handler"
  (get-event-handler :task-slot-created :domain) => (complement nil?)

  (count (get-event-handler-with-exclusion :task-slot-created :a)) => 2

  (get-event-handler-with-exclusion :task-slot-created :domain) =>
          [raiseup.ontime.readmodel/task-slot-created])
