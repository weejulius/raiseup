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
  [command-type f get-bus get-snapshot-db]
  (p/reg (get-bus) command-type
         (fn [command]
           (log/debug "handle command" command)
           (let [snapshot-db (get-snapshot-db)
                 bus (get-bus)
                 ar (get-ar (:ar command) (:ar-id command) snapshot-db)
                 event (f ar command)
                 snapshot (get-ar [ar event])]
             (es/store-snapshot snapshot snapshot-db)
             (log/debug "publishing event" event ar command snapshot)
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

(defn- validate-schema
  [schema any]
  (schema/validate schema any))

(defn gen-command
  "generate command"
  [ar command-type fields recoverable-ids]
  (let [command (assoc fields :command command-type)
        command (assoc command :ar ar)
        schema (get @schemas command-type)]
    (if (nil? schema)
      (throw (ex-info "schema is missing"
                      {:command command-type
                       :fields  fields
                       :schemas @schemas}))
      (do (validate-schema schema command)
          (if-not (:ar-id command)
            (assoc command :ar-id (inc-id-for (str (:ar command)) recoverable-ids))
            command)))))


;;elastic search fetch
(defn fetch
  "fetch result of query"
  [readmodel query]
  (if-not (:ar query)
    (throw (ex-info "ar is missing for the query"
                    {:query query})))
  (validate-schema (type query) query)
  (if (:id query)
    (p/load-entry
      readmodel (:ar query) (:id query))
    (let [p (or (:page query) 1)
          s (or (:size query) 20)
          basic-query [:from (* s (dec p))
                       :size s]
          more (p/query query)
          combined (concat basic-query (flatten (seq more)))
          combined (if-not (:sort more)
                     (concat combined [:sort {:ar-id "asc"}])
                     combined)]
      (p/do-query
        readmodel
        (:ar query)
        combined))))