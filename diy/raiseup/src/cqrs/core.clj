(ns ^{:doc
             " # the lib is utilized to apply the CQRS pattern, here it will do the following

             * the channels are registered for event/command, and binded to handler to process
               the comming event/command automatically
             * the channel for command will store and emit the produced events after handling it
             * the event might have serveral channels to server it, like a channel to update readmodel
             * the readmodel is used to offer content to be queried "
      :added "1.0"}
  cqrs.core
  (:require [cqrs.eventstore :as es]
            [cqrs.protocol :as p]
            [cqrs.storage :as storage]
            [clojure.core.reducers :as r]
            [common.func :as func]
            [common.logging :as log]
            [common.convert :as convert]
            [schema.core :as schema]
            [clojure.core.async :as async :refer [<! <!! >! put! go chan timeout alts!]]))

;;deprecate
(defn- read-ar-events
  "read aggregate root events"
  [ar-name ar-id event-ids-db events-db]
  (es/read-events ar-name ar-id event-ids-db events-db))


(defn get-ar
  "retrieve the aggregate root state by replaying its events for ar
   or get the snapshot"
  ([events]
   (reduce
     (fn [m v]
       (if (empty? v) m
                      (merge
                        (assoc m :vsn (get v :vsn (inc (get m :vsn 0))))
                        v)))
     {}
     events))
  ([ar-name ar-id snapshot-db]
   (es/retreive-ar-snapshot ar-name ar-id snapshot-db)))

(defn inc-id-for
  "increase the id for the key which is a kind of ar, or the event"
  ([^String key recoverable-ids]
   (storage/inc! recoverable-ids key)))

(defn gen-event
  ^{:doc   "generate event from cmd"
    :added "1.0"}
  [event-type cmd keys]
  (let [event {:event       event-type
               :ar          (:ar cmd)
               :ar-id       (:ar-id cmd)
               :event-ctime (convert/->long (java.util.Date.))}]
    (merge event (select-keys cmd keys))))


(defn publish-event
  "publish event"
  [bus event]
  (p/publish bus event))


(defn register-command-handler
  "register command handler, the command will find the handler from the registry"
  [command-type f bus snapshot-db]
  (p/reg bus command-type
         (fn [command]
           (println "handle command" command)
           (let [ar (get-ar (:ar command) (:ar-id command) snapshot-db)
                 event (f ar command)
                 snapshot (get-ar [ar event])]
             (es/store-snapshot snapshot snapshot-db)
             (println "publishing event" event ar command snapshot)
             (publish-event bus event)))))


(defn register-event-handler
  "register event handler"
  [event-type f bus readmodel]
  (p/reg bus event-type
         (fn [event]
           (f event readmodel))))


(defn send-command
  "send commands"
  [bus command]
  (p/sends bus command)
  (:ar-id command))



(defonce schemas (atom {}))

(defn def-schema
  "define schema which can be got by name"
  [name schema]
  (do
    (log/debug "def schema" name)
    (swap! schemas #(assoc % name schema))))

(defn gen-command
  "generate command"
  [command-type fields recoverable-ids]
  (let [command (assoc fields :command command-type)
        schema (get @schemas command-type)]
    (if (nil? schema)
      (throw (ex-info "schema is missing"
                      {:command command-type
                       :fields fields
                       :schemas @schemas}))
      (do (schema/validate schema command)
          (if-not (:ar-id command)
            (assoc command :ar-id (inc-id-for (str (:ar command)) recoverable-ids))
            command)))))
