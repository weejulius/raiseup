(ns raiseup.ontime.readmodel
  (:require [common.convert :as convert]
            [cqrs.protocol :refer [on-event]]))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (put-in-readmodel (:ar event) ar-id event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))]
    (remove-from-readmodel (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (update-in-readmodel (:ar event)
                       (:ar-id event)
                       #(assoc % :start-time (:start-time event))))
