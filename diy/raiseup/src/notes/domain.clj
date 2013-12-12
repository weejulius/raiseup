(ns
    ^{:doc "notes is a short of words to record"}
    notes.domain
    (:require [system :as s]))

(defn create-note
  [note]
  (s/gen-event :note-created note
                  [:author :title :content :ctime]))

(defn update-note
  [note changes]
  [note (s/gen-event :note-updated changes
                        [:author :title :content :utime])])

(defn delete-note
  [note changes]
  [note (s/gen-event :note-deleted changes [])])
