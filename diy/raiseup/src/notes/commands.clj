(ns notes.commands
  (:require [cqrs.protocol :as cqrs]
            [bouncer [core :as b] [validators :as v]])
  (:gen-class))

(defrecord CreateNote [ar author title content ctime]
  cqrs/Validatable
  (validate [cmd]
    (b/validate cmd
                :title v/required
                :content v/required)))


(defrecord UpdateNote [ar ar-id author title content utime]
  cqrs/Validatable
  (validate [cmd]
    (b/validate cmd
                :ar-id v/required)))
