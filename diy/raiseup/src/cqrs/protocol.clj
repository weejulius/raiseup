(ns ^{:doc "the cqrs basic fun"
      :added "1.0"}
  cqrs.protocol
  (:require [cqrs.eventstore :as es]
            [cqrs.storage :as storage]
            [clojure.core.reducers :as r]
            [common.config :as cfg]))

(def eventids-db-path (cfg/ret :es :event-id-db-path))
(def events-db-path (cfg/ret :es :events-db-path))
(def event-identifier (cfg/ret :es :recoverable-event-id-key))
(def ar-identifier (cfg/ret :es :recoverable-ar-id-key))
(def default-leveldb-option (cfg/ret :leveldb-option))

(def event-ids-db (storage/init-store eventids-db-path
                                      default-leveldb-option))

(def events-db  (storage/init-store events-db-path
                                    default-leveldb-option))

(def event-id-creator
  (storage/init-recoverable-long-id event-identifier
                                    event-ids-db))

(def ar-id-creator (storage/init-recoverable-long-id
                    ar-identifier
                    event-ids-db))


(defn- read-ar-events
  "read aggregate root events"
  [ar-name ar-id]
  (es/read-events ar-name ar-id event-ids-db events-db))

(defn get-ar
  "retrieve the aggregate root state by replay events for ar"
  ([events get-handler]
     (r/reduce
      (fn [state event]
        ((get-handler (:event event) :domain) state event)) {} events))
  ([ar-name ar-id fn-get-event-handler]
     (get-ar (read-ar-events ar-name ar-id)
             fn-get-event-handler)))

(defn- populate-id-if-need
  "if the id is not existing one is given to command"
  [command]
  (if (nil? (:ar-id command))
    (assoc command :ar-id (.inc! ar-id-creator))
    command))


(defn- send-event
  ^{:doc "send events to their listeners, if the execution
          of event does not take much time, do not use pararellasm"}
  [event fn-get-event-handlers]
  (doseq [handler
          (fn-get-event-handlers event)]
    (handler event)))

(defprotocol Validatable
  "the validation definition"
  (validate [this] "validate the command"))

(defmulti handle-command
  ^{:doc "handle the  command"
    :added "1.0"}
  (fn [command] (type command)))

(defn gen-event
  ^{:doc "generate event from cmd"
    :added "1.0"}
  [event-type cmd & keys]
  (let [event {:event event-type
          :ar (:ar cmd)
          :ar-id (:ar-id cmd)
          :etc (java.util.Date.)}]
        (reduce #(assoc % %2 (%2 cmd)) event keys)))

(defn- validate-command
  "validate command"
  [command]
  (if-not (extends? Validatable (type command))
    {:ok? true :result command}
    (let [errors (first (.validate command))]
      (if (nil? errors)
        {:ok? true :result command}
        {:ok? false :result (vals errors)}))))

(defn send-command
  "send command to bus"
  [command fn-get-event-handlers]
  (let [command-with-id (populate-id-if-need command)
        validated-command (validate-command command-with-id)]
    (if-not (:ok? validated-command) validated-command
            (let [produced-events
                  (handle-command (:result validated-command))
                  events-with-id
                  (map
                   #(assoc % :event-id (.inc! event-id-creator))
                   [produced-events])]
              (es/store-events (:ar command-with-id)
                               (:ar-id command-with-id)
                               events-with-id
                               event-ids-db
                               events-db)
              (dorun (map #(send-event %  fn-get-event-handlers) events-with-id))
              (:ar-id command-with-id)))))
