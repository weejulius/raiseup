(ns system
  (:import (clojure.lang IPersistentMap))
  (:require [env :as env]
            [cqrs.core :as cqrs]
            [cqrs.protocol :refer :all]
            [common.logging :as log]))

(defonce system (env/->NoteSystem))

(defn send-command
  [command & {:as options}]
  (let [bus (:command-bus system)]
    (sends bus command options)))
