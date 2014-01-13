(ns ^{:doc   "the commands"
      :added "1.0"}
  ontime.commands
  (:require [cqrs.protocol :as cqrs]
            [bouncer [core :as b]
            [validators :as v]])
  (:gen-class))

(defrecord CreateTaskSlot [ar ar-id user-id description start-time estimation]
  cqrs/Validatable
  (validate [cmd]
    (b/validate cmd :description v/required)))

(defrecord DeleteTaskSlot [ar ar-id user-id])

(defrecord StartTaskSlot [ar ar-id start-time]
  cqrs/Validatable
  (validate [cmd]
    (b/validate cmd
                :ar-id [v/required]
                :start-time v/required)))
