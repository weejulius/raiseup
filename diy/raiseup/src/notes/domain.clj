(ns
    ^{:doc "notes is a short of words to record"}
    notes.domain
    (:require [cqrs.core :as cqrs]))

(defn create-note
  [cmd]
  (cqrs/gen-event :note-created cmd [:author :title :content :ctime]))

(defn update-note
  [note cmd]
  [note (cqrs/gen-event :note-updated cmd
                        [:author :title :content :utime])])

(defn delete-note
  [note cmd]
  [note (cqrs/gen-event :note-deleted cmd [])])
