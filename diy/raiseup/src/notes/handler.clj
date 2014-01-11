(ns notes.handler
  (:require [cqrs.protocol :refer [CommandHandler on-event]]
            [notes.domain :refer :all]
            [cqrs.core :as cqrs]
            [common.logging :as log]
            [taoensso.timbre.profiling :as p])
  (:import (notes.commands CreateNote
                           UpdateNote
                           DeleteNote)))

(defn- ar-is-required
  [ar cmd]
  (if (empty? ar)
      (throw (ex-info "ar not found"
                      {:ar (:ar cmd)
                       :ar-id (:ar-id cmd)}))))

(extend-protocol CommandHandler
  CreateNote
  (handle-command [cmd ar]
    (let []
      [{} (create-note cmd)]))

  UpdateNote
  (handle-command [cmd ar]
    (ar-is-required ar cmd)
    (update-note ar cmd))

  DeleteNote
  (handle-command [cmd ar]
    (ar-is-required ar cmd)
    (delete-note ar cmd)))

(defmethod on-event
  :note-created
  [event readmodel]
  (.put-entry readmodel
               (select-keys event [:ar :ar-id :author :title :content :ctime])))

(defn- update-fn
  [cur-entry event keys]
  (reduce
    (fn [m k]
      (assoc m k (or (k event) (k m))))
    cur-entry
    keys))

(defmethod on-event
  :note-updated
  [event readmodel]
  (do
    (.update-entry
     readmodel
     (:ar event)
     (:ar-id event)
     #(update-fn % event [:author :title :content :utime]))))


(defmethod on-event
  :note-deleted
  [event readmodel]
  (.remove-entry
     readmodel
     (:ar event)
     (:ar-id event)))
