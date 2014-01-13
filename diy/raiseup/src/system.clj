(ns system
  (:require [env :refer :all]
            [cqrs.core :as cqrs]
            [cqrs.protocol :refer :all]
            [common.logging :as log])
  (:import [clojure.lang IPersistentMap]
           [env NoteSystem]))

(defonce system (NoteSystem.))

(defn send-command
  [command & {:as options}]
  (let [bus (:command-bus system)]
    (sends bus command options)))
