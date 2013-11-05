(ns ontime.readmodel
  (:require [common.convert :as convert]
            [ontime :refer [entries]]
            [cqrs.protocol :refer [on-event]])
  (:gen-class))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (.put-entry entries event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))]
    (.remove-entry entries (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (.update-entry entries
                 (:ar event)
                 (:ar-id event)
                 #(assoc % :start-time (:start-time event))))
