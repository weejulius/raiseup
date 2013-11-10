(ns
    ^{:doc "notes is a short of words to record"}
    notes.domain
    (:require [cqrs.core :as cqrs]))

(defn create-note
  [note]
  (cqrs/gen-event :note-created note
                  [:author :title :content :ctime]))
