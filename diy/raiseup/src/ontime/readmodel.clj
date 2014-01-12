(ns ontime.readmodel
  (:require [common.convert :as convert]
            [cqrs.protocol :refer [on-event]]
            [system :as s])
  (:import (cqrs.protocol ReadModel))
  (:gen-class))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)
        ^ReadModel rm (:readmodel s/system)]
    (.put-entry rm event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))
        ^ReadModel rm (:readmodel s/system)]
    (.remove-entry rm (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (let [ar-id (convert/->long (:ar-id event))
        ^ReadModel rm (:readmodel s/system)]
    (.update-entry  rm
                   (:ar event)
                   (:ar-id event)
                   #(assoc % :start-time (:start-time event)))))
