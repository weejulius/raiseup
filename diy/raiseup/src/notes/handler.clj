(ns notes.handler
  (:require [cqrs.protocol :refer [CommandHandler on-event]]
            [notes.domain :refer :all]
            [system :refer [entries]])
  (:import (notes.commands CreateNote)))


(extend-type CreateNote
  CommandHandler
  (handle-command [cmd]
    [nil (create-note cmd)]))


(defmethod on-event
  :note-created
  [event]
  (.put-entry entries
              (select-keys event [:ar :ar-id :author :title :content :ctime])))
