(ns ontime.readmodel
  (:require [common.convert :as convert]
            [cqrs.protocol :refer [on-event] :as p]
            [system :as s]))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)
        rm (:readmodel s/system)]
    (p/put-entry rm event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))
        rm (:readmodel s/system)]
    (p/remove-entry rm (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (let [ar-id (convert/->long (:ar-id event))
        rm (:readmodel s/system)]
    (p/update-entry  rm
                   (:ar event)
                   (:ar-id event)
                   #(assoc % :start-time (:start-time event)))))
