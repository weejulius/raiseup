(ns system
  (:require [env :as env]
            [cqrs.core :as cqrs]
            [common.logging :as log]))

(defonce system (env/->NoteSystem))

(defn get-ar
  [ar ar-id]
  (cqrs/get-ar ar ar-id (:snapshot-db system)))

(defn send-command
  [command & {:as options}]
  (do (log/debug options)
    (.sends (:command-bus system) command options)))

(defn gen-event
  ^{:doc "generate event from cmd"
    :added "1.0"}
  [event-type cmd keys]
  (let [event-id (cqrs/inc-id-for :event
                                  (:id-creators system)
                                  (:recoverable-id-db system))
        event    {:event       event-type
                  :event-id    event-id
                  :ar          (:ar cmd)
                  :ar-id       (:ar-id cmd)
                  :event-ctime (java.util.Date.)}]
    (merge event (select-keys cmd keys))))
