(ns ^{:doc "utilizes applying the CQRS pattern,
            including the event/command bus and event store"
      :added "1.0"}
  cqrs.core
  (:require [cqrs.eventstore :as es]
            [cqrs.protocol :refer :all]
            [cqrs.storage :as storage]
            [clojure.core.reducers :as r]
            [common.cache :as cache]
            [common.func :as func]
            [common.logging :as log]
            [system :as s]
            [clojure.core.async :as async :refer [<! >! go chan]]))


(defn- read-ar-events
  "read aggregate root events"
  [ar-name ar-id event-ids-db events-db]
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
  [command ar-id-creator]
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



(defn register-channel
  "register a channel, the channel listenes to event/command
   and responsible for handing them.
   the channels are mapped as {$:type {$:from channel}}"
  [channel-map type from handler]
  (func/put-if-absence!
   channel-map [type from]
   (fn [f]
     (let [ch (chan)]
       (log/debug "register channel" type from ch)
       (go (while true
             (let [cmd (<! ch)]
               (f cmd))))
       ch))))


(defn emit
  "emit event/command to the listening channel, type is to find channels related to the type"
  [channel-map event type]
  (let [chs (get-in channel-map type)]
    (if (nil? chs) (throw "no any handler for event" event "type" type)
        (doseq [[key ch] chs]
          (go (>! ch event))))))


(defn- emit-command
  "register a channel if the command does not have,
   and emit the command to the channel"
  [channel-map handle-command-fn cmd]
  (do
    (register-channel channel-map [(type cmd) :command] handle-command-fn)
    (emit channel-map cmd (type cmd))))

(defn prepare-and-emit-event
  "emit the event, but register channel for the event if unregistered"
  [channel-map event]
  (let [event-type (:event event)]
    (register-channel channel-map  event-type [(str on-event)] on-event)
    (emit event event-type)))

(defn- validate-command
  "validate command"
  [command]
  (if-not (extends? Validatable (type command))
    {:ok? true :result command}
    (let [errors (first (.validate command))]
      (if (nil? errors)
        {:ok? true :result command}
        {:ok? false :result (vals errors)}))))


(defn- process-command
  "handle the command , meanwhile store
   and emit the events produced by command to their channel "
  [command channel-map event-ids-db events-db event-id-creator]
  (let [events-with-id (->> [(.handle-command command)]
                            (map #(assoc % :event-id (.inc! event-id-creator))))]
    (es/store-events (:ar command)
                     (:ar-id command)
                     events-with-id
                     event-ids-db
                     events-db)
    (dorun (map #(prepare-and-emit-event channel-map %) events-with-id))))

(defn send-command
  "send command to channel, the channel will handle the command,
   and then store and emit the produced events"
  ([command channel-map event-ids-db events-db event-id-creator ar-id-creator]
     (let [command-with-id (populate-id-if-need command ar-id-creator)
           validated-command (validate-command command-with-id)]
       (if-not (:ok? validated-command) validated-command
               (emit-command
                (fn [cmd]
                  (process-command cmd channel-map
                                   event-ids-db events-db event-id-creator))
                (:result validated-command)))
       (:ar-id command-with-id)))
  ([command]
     (send-command
      (:channels s/system)
      (:event-ids-db s/system)
      (:events-db s/system)
      ((:event-id-creator s/system) s/system)
      ((:ar-id-creator s/system) s/system))))
