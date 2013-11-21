(ns notes.handler
  (:require [cqrs.protocol :refer [CommandHandler on-event]]
            [notes.domain :refer :all]
            [system :as s]
            [cqrs.core :as cqrs]
            [common.logging :as log])
  (:import (notes.commands CreateNote
                           UpdateNote)))


(extend-type CreateNote
  CommandHandler
  (handle-command [cmd]
    (let []
      (Thread/sleep 200)
      [{} (create-note cmd)])))

(extend-type UpdateNote
  CommandHandler
  (handle-command [cmd]
    (let [ar (s/get-ar (:ar cmd) (:ar-id cmd))]
      (if (empty? ar)
        (throw (ex-info "ar not found"
                        {:ar (:ar cmd)
                         :ar-id (:ar-id cmd)}))
        (update-note ar cmd)))))


(defmethod on-event
  :note-created
  [event]
  (do
    (.put-entry (:readmodel s/system)
                (select-keys event [:ar :ar-id :author :title :content :ctime]))))

(defn- update-fn
  [cur-entry event keys]
  (do (log/debug "updating " cur-entry event keys)
   (reduce
    (fn [m k]
      (assoc m k (or (k event) (k m))))
    cur-entry
    keys)))

(defmethod on-event
  :note-updated
  [event]
  (do
    (.update-entry
     (:readmodel s/system)
     (:ar event)
     (:ar-id event)
     #(update-fn % event [:author :title :content :utime]))))
