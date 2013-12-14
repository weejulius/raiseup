(ns system
  (:require [env :as env]
            [cqrs.core :as cqrs]
            [common.logging :as log]))

(defonce system (env/->NoteSystem))

(defn send-command
  [command & {:as options}]
  (.sends (:command-bus system) command options))
