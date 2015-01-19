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
             (log/debug "stored and to event" event command)
             (publish-event bus event)))))


(defn register-event-handler
  "register event handler"
  [event-type f bus readmodel]
  (p/reg bus event-type
         (fn [event]
           (f event readmodel))))


(defn send-command
  "send commands"
  [bus command options]
  (p/sends bus command options)
  (:ar-id command))



(defonce defs-cache (atom {}))

(defn def-command-schema
  "define schema which can be got by name"
  [name schema]
  (do
    (log/debug "command schema" name)
    (swap! defs-cache
           #(assoc % name
                     (merge
                       {:ar      schema/Keyword
                        :command schema/Keyword}
                       schema)))))

(defn def-query
  "define schema which can be got by name"
  [ar defs]
  (swap! defs-cache
         #(assoc % (str "query-" ar "-schema")
                   (merge
                     (:schema defs)
                     {
                       (schema/optional-key :ar-id) (schema/maybe schema/Num)
                       (schema/optional-key :page)  (schema/maybe schema/Num)
                       (schema/optional-key :size)  (schema/maybe schema/Num)})))
  (swap! defs-cache
         #(assoc % (str "query-" ar) (:query defs))))


(defn- validate-schema
  [schema any]
  (schema/validate schema any))

(defn ar-is-required
  [ar cmd]
  (if (empty? ar)
    (throw (ex-info "ar not found"
                    {:ar      (:ar cmd)
                     :ar-id   (:ar-id cmd)
                     :command cmd}))))

(defn gen-command
  "generate command"
  ([ar command-type fields recoverable-ids snapshot-db]
   (gen-command (-> fields
                    (assoc :ar ar)
                    (assoc :command command-type))
                recoverable-ids
                snapshot-db))

  ([command recoverable-ids snapshot-db]
   (let [command-type (:command command)
         schema (get @defs-cache command-type)]
     (if (nil? schema)
       (throw (ex-info "schema is missing"
                       {:type    command-type
                        :command command
                        :schemas @defs-cache}))
       (let [ar-id (:ar-id command)
             ar-name (:ar command)]
         (validate-schema schema command)
         (if (nil? ar-id)
           (assoc command :ar-id
                          (inc-id-for (str ar-name) recoverable-ids))
           (let [ar (get-ar (name ar-name) ar-id snapshot-db)]
             (ar-is-required ar command)
             command)))))))


;;elastic search fetch
(defn fetch
  "fetch result of query"
  [readmodel ar query]
  (if (nil? ar)
    (throw (ex-info "ar is missing for the query"
                    {:query query})))
  (validate-schema (get @defs-cache (str "query-" ar "-schema")) query)
  (if (:ar-id query)
    (p/load-entry
     readmodel ar (:ar-id query))
    (let [p (or (:page query) 1)
          s (or (:size query) 20)
          basic-query [:from (* s (dec p))
                       :size s]
          query-fn (get @defs-cache (str "query-" ar))
          more-query (query-fn query) ;; get sql from cache
          combined (apply concat basic-query more-query)
          combined (if-not (:sort more-query)
                     (concat combined [:sort {:ar-id "desc"}])
                     combined)]
      (p/do-query readmodel
                  ar
                  combined))))



