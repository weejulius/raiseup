(ns ^{:doc "
            # the lib is utilized to apply the CQRS pattern, here it will do the following
            * the channels are registered for event/command, and binded to handler to process
              the comming event/command automatically
            * the channel for command will store and emit the produced events after handling it
            * the event might have serveral channels to server it, like a channel to update readmodel
            * the readmodel is used to offer content to be queried "
      :added "1.0"}
  cqrs.core
  (:require [cqrs.eventstore :as es]
            [cqrs.protocol :refer :all]
            [cqrs.storage :as storage]
            [clojure.core.reducers :as r]
            [common.func :as func]
            [common.logging :as log]
            [system :as s]
            [common.convert :as convert]
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
     (es/retreive-ar-snapshot ar-name ar-id snapshot-db))
  ([ar-name ar-id]
     (get-ar ar-name ar-id (:snapshot-db s/system))))

(defn- inc-id-for
  "increase the id for the key which is a kind of ar, or the event"
  ([key id-creators recoverable-id-db]
     (let [id-name (name key)
           id-creator
           (get-in
            (func/put-if-absence! id-creators
                                  [id-name]
                                  (fn []
                                    (storage/init-recoverable-long-id
                                     id-name
                                     recoverable-id-db)))
            [id-name])]
       (.inc! id-creator)))
  ([key]
     (inc-id-for key (:id-creators s/system) (:recoverable-id-db s/system))))

(defn- populate-command-id-if-need
  "if the id is not existing one is given to command"
  [command id-creators recoverable-id-db]
  (if (nil? (:ar-id command))
    (assoc command :ar-id
           (inc-id-for (:ar command) id-creators recoverable-id-db))
    command))



(defn gen-event
  ^{:doc "generate event from cmd"
    :added "1.0"}
  [event-type cmd keys]
  (let [event-id (inc-id-for :event)
        event
        {:event event-type
         :event-id event-id
         :ar (:ar cmd)
         :ar-id (:ar-id cmd)
         :event-ctime (java.util.Date.)}]
    (merge event (select-keys cmd keys))))



(defn register-channel
  "register a channel, the channel listenes to event/command
   and responsible for handing them.
   the channels are mapped as {$:type {$:from channel}}"
  [channel-map type from handler]
  (func/put-if-absence!
   channel-map [type from]
   (fn []
     (let [input-ch (chan)
           output-ch (chan)]
       (log/debug "register channel" type from input-ch output-ch)
       (go (let []
             (loop []
               (when-let [cmd (<! input-ch)]
                 (log/debug "receiving " cmd)
                 (try
                   (handler cmd)
                   (catch Exception e
                     (log/error e)
                     e))
                 (>! output-ch "")
                 (recur))
               (log/debug "shutdown channel " type from))))
       [input-ch output-ch]))))


(defn emit
  "emit event/command to the listening channel,
   type is to find channels related to the type"
  [channel-map event event-type options]
  (if-not (empty? event)
    (let [chs (get @channel-map  event-type)]
      (if (empty? chs)
        (do (throw
             (Exception.
              (str "no any handler for event " event " type " event-type))))
        (let [timeout-ms (:timeout options)
              ch-seq     (vals chs)
              output-chs (map (fn [[input-ch output-ch]]
                                (let []
                                  (go (>! input-ch event))
                                  output-ch))
                              ch-seq)]
          (log/debug "emitting " event "with options " options output-chs ch-seq)
          (if-not (nil? timeout-ms)
            (<!!
             (go (alts! (vec (conj output-chs (timeout timeout-ms))))))
            (doseq [output-ch output-chs]
              (go (<! output-ch)))))))))


(defn- emit-command
  "register a channel if the command does not have,
   and emit the command to the channel"
  [channel-map handle-command-fn cmd options]
  (let [command-type (str (type cmd))]
    (register-channel channel-map command-type :command handle-command-fn)
    (emit channel-map cmd command-type options)))

(defn prepare-and-emit-event
  "emit the event, but register channel for the event if unregistered"
  [channel-map event options]
  (let [event-type (:event event)]
    (register-channel channel-map  event-type (str on-event) on-event)
    (emit channel-map event event-type options)))

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
  [command channel-map snapshot-db events-db]
  (let [handle-result (handle-command command)
        old-snapshot (first handle-result)
        new-events (rest handle-result)
        new-snapshot (get-ar handle-result)]
    (es/store-snapshot-and-events new-snapshot
                                  new-events
                                  snapshot-db
                                  events-db)
    (dorun (map #(prepare-and-emit-event channel-map % {}) new-events))))


(defn replay-events
  [store]
  (let []
    (log/info "[=>]replaying events to rebuild the state of entries")
    (.map
     store
     (fn [k v]
       (prepare-and-emit-event (:channels s/system) (convert/->data v) {})))
    (log/info "[<=]replayed events")))

(defn fetch
  "fetch result of query"
  [query]
  (if (:id query)
    (.find-by-id query)
    (.query query)))


(defrecord SimpleCommandBus [channel-map snapshot-db events-db  id-creators recoverable-id-db]
  CommandBus
  (sends [this command options]
    (let [command-with-id (populate-command-id-if-need
                           command id-creators recoverable-id-db)
          validated-command (validate-command command-with-id)]
      (if-not (:ok? validated-command) validated-command
              (emit-command
               channel-map
               (fn [cmd]
                 (process-command cmd channel-map
                                  snapshot-db events-db))
               (:result validated-command)
               options))
      (:ar-id command-with-id)))
  (register [this command handler]
    (register-channel channel-map (type command) handler)))


(def simple-commandbus
   (->SimpleCommandBus
   (:channels s/system)
   (:snapshot-db s/system)
   (:events-db s/system)
   (:id-creators s/system)
   (:recoverable-id-db s/system)))

(defn send-command
  [command & {:as options}]
  (do (log/debug options)
    (.sends simple-commandbus command options)))
