(ns ontime.readmodel
  (:require [common.convert :as convert]
            [cqrs.protocol :as p]
            [system :as s]))

(s/register-event-handler :task-slot-created
                          (fn [event readmodel]
                            (p/put-entry readmodel event)))


(s/register-event-handler :task-slot-deleted
                          (fn [event readmodel]
                            (p/remove-entry readmodel (:ar event) (:ar-id event))))


(s/register-event-handler :task-slot-started
                          (fn [event readmodel]
                            (p/update-entry readmodel
                                            (:ar event)
                                            (:ar-id event)
                                            #(assoc % :start-time (:start-time event)))))
