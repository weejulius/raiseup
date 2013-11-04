(ns ^{:doc "utilizes applying the CQRS pattern,
            including the event/command bus and event store"
      :added "1.0"}
  cqrs.core
  (:require [cqrs.eventstore :as es]
            [cqrs.protocol :refer :all]
            [cqrs.storage :as storage]
            [clojure.core.reducers :as r]
            [common.config :as cfg]
            [common.cache :as cache]
            [clojure.core.async :as async :refer [<! >! go chan]]))


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



(defn- populate-id-if-need
  "if the id is not existing one is given to command"
  [command]
  (if (nil? (:ar-id command))
    (assoc command :ar-id (.inc! ar-id-creator))
    command))


(defn gen-event
  ^{:doc "generate event from cmd"
    :added "1.0"}
  [event-type cmd & keys]
  (let [event {:event event-type
          :ar (:ar cmd)
          :ar-id (:ar-id cmd)
          :ctime (java.util.Date.)}]
        (reduce #(assoc % %2 (%2 cmd)) event keys)))

(defn get-ar
  "retrieve the aggregate root state by replay events for ar"
  ([events get-handler]
     (r/reduce
      (fn [state event]
        ((get-handler (:event event) :domain) state event)) {} events))
  ([ar-name ar-id fn-get-event-handler]
     (get-ar (read-ar-events ar-name ar-id)
             fn-get-event-handler)))

(defn register-handler
  "register the channel to listen event/command and handle the comming ones"
  [event-type f]
  (let [ch (cache/get-cache event-type [*ns*] chan)]
    (go (while true
          (let [cmd (<! ch)]
            (f cmd))))))


(defn emit
  "emit event/command to the listening channel"
  [event]
  (let [chs (cache/get-cache (or (:event event) (type event)) (fn [] nil))]
    (if (nil? chs) (throw "no handler for event" event)
        (doseq [[key ch] chs]
          (go (>! ch event))))))


(defn- asyn-handle-command
  "register the unregistered command before emitting "
  [handle-command-fn cmd]
  (let []
    (register-handler (type cmd) handle-command-fn)
    (emit cmd)))

(defn- validate-command
  "validate command"
  [command]
  (if-not (extends? Validatable (type command))
    {:ok? true :result command}
    (let [errors (first (.validate command))]
      (if (nil? errors)
        {:ok? true :result command}
        {:ok? false :result (vals errors)}))))

(defn prepare-and-emit-event
  "emit the event, but register handler for the event if unregistered"
  [event]
  (let [chs (cache/get-cache (:event event) (fn []  nil))]
    (if (nil? chs) (register-handler (:event event) on-event))
    (emit event)))

(defn send-command
  "send command to channel, the channel will handle the command,
   and then store and emit the produced events"
  [command]
  (let [command-with-id (populate-id-if-need command)
        validated-command (validate-command command-with-id)]
    (if-not (:ok? validated-command) validated-command
            (asyn-handle-command
             (fn [c]
               (let [produced-events [(handle-command (:result c))]
                     events-with-id
                     (map
                      #(assoc % :event-id (.inc! event-id-creator)) produced-events)]
                 (es/store-events (:ar command-with-id)
                                  (:ar-id command-with-id)
                                  events-with-id
                                  event-ids-db
                                  events-db)
                 (dorun (map #(prepare-and-emit-event %) events-with-id))))
             validated-command))
    (:ar-id command-with-id)))
