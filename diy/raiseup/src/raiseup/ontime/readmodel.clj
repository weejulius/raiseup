(ns raiseup.ontime.readmodel
  (:require [common.convert :as convert]
            [ontime :refer [models]]
            [cqrs.protocol :refer [on-event]]))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (.put-entry models event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))]
    (.remove-entry models (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (.update-entry models
                 (:ar event)
                 (:ar-id event)
                 #(assoc % :start-time (:start-time event))))
