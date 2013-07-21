(ns raiseup.cqrsroutes
  (:require [raiseup.ontime.commands :as commands]
            [raiseup.ontime.domain :as domain]
            [raiseup.ontime.readmodel :as readmodel]))

(def command-routes
  {:create-task-slot commands/create-task-slot
   :delete-task-slot commands/delete-task-slot
   :start-task-slot commands/start-task-slot} )

(def event-routes
  {:task-slot-created {:domain [domain/task-slot-created]
                       :readmodel [readmodel/task-slot-created]}
   :task-slot-deleted {:domain [domain/task-slot-deleted]
                       :readmodel [readmodel/task-slot-deleted]}
   :task-slot-started {:domain [domain/task-slot-started]
                       :readmodel [readmodel/task-slot-started]}})


(defn get-event-handler
  "get event handler from router, the comp is where the event handler belong to,
   eg. read model, domain etc."
  [event-type comp]
  (get-in event-routes [event-type comp]))

(defn get-event-handler-with-exclusion
  "get the event handler from router, but exclude ones from some compoment"
  ([event-type comp a-event-routes]
     (into [] (flatten (vals (dissoc (a-event-routes event-type) comp)))))
  ([event-type comp]
      (get-event-handler-with-exclusion event-type comp event-routes)))
