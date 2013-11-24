(ns ontime.readmodel
  (:require [common.convert :as convert]
            [cqrs.protocol :refer [on-event]]
            [system :as s])
  (:gen-class))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (.put-entry (:readmodel s/system)
                event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))]
    (.remove-entry (:readmodel s/system)
                   (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (.update-entry (:readmodel s/system)
                 (:ar event)
                 (:ar-id event)
                 #(assoc % :start-time (:start-time event))))
