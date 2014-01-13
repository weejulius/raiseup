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
            [cqrs.protocol :refer [Validatable on-event] :as p]
            [cqrs.storage :as storage]
            [clojure.core.reducers :as r]
            [common.func :as func]
            [common.logging :as log]
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
     (es/retreive-ar-snapshot ar-name ar-id snapshot-db)))

(defn inc-id-for
  "increase the id for the key which is a kind of ar, or the event"
  ([key id-creators recoverable-id-db]
     (let [id-name (name key)
           id-creator
           (get-in (func/put-if-absence! id-creators
                                         [id-name]
                                         (fn []
                                           (storage/init-recoverable-long-id
                                            id-name
                                            recoverable-id-db)))
                   [id-name])]
       (storage/inc! id-creator))))

(defn gen-event
  ^{:doc "generate event from cmd"
    :added "1.0"}
  [event-type cmd keys]
  (let [event    {:event       event-type
                  :ar          (:ar cmd)
                  :ar-id       (:ar-id cmd)
                  :event-ctime (java.util.Date.)}]
    (merge event (select-keys cmd keys))))

(defn- populate-command-id-if-need
  "if the id is not existing one is given to command"
  [command id-creators recoverable-id-db]
  (if (nil? (:ar-id command))
    (assoc command :ar-id
           (inc-id-for (:ar command) id-creators recoverable-id-db))
    command))

(defn register-channel
  "register a channel, the channel listenes to event/command
   and responsible for handing them.
   the channels are mapped as {$:type {$:from channel}}"
  [channel-map type from handler]
  (func/put-if-absence!
   channel-map [type from]
   (fn []
     (let [ch (chan)]
      ; (log/debug "register channel" type from ch)
       (go (let []
             (loop []
               (when-let [[cmd output-ch](<! ch)]
                ; (log/debug "receiving " cmd)
                 (try
                   (handler cmd)
                   (catch Exception e
                     (log/error e)
                     e))
                 (if-not (nil? output-ch) (>! output-ch ""))
                 (recur))
               (log/debug "shutdown channel " type from))))
       ch))))


(defn emit
  "emit event/command to the listening channel,
   type is to find channels related to the type"
  [channel-map event event-type options]
  (if-not (empty? event)
    (let [chs (get @channel-map  event-type)]
      (if (empty? chs)
        (throw
             (Exception.
              (str "no any handler for event " event " type " event-type)))
        (let [timeout-ms (:timeout options)
              ch-seq     (vals chs)]
         ; (log/debug "emitting " event "with options " options ch-seq)
          (if-not (nil? timeout-ms)
            (let [output-chs  (map (fn [ch]
                                     (let [output-ch (chan 1)]
                                       (put! ch [event output-ch])
                                       output-ch))
                                   ch-seq)]
              (<!! (go (alts! (vec (conj output-chs (timeout timeout-ms)))))))
            (doseq [ch ch-seq]
              (put! ch [event nil]))))))))


(defn- emit-command
  "register a channel if the command does not have,
   and emit the command to the channel"
  [channel-map handle-command-fn cmd options]
  (let [command-type (str (type cmd))]
    (register-channel channel-map command-type :command handle-command-fn)
    (emit channel-map cmd command-type options)))

(defn prepare-and-emit-event
  "emit the event, but register channel for the event if unregistered"
  [channel-map readmodel event options]
  (let [event-type (:event event)]
    (register-channel channel-map  event-type (str on-event)
                      #(on-event % readmodel))
    (emit channel-map event event-type options)))

(defn- validate-command
  "validate command"
  [command]
  (if-not (:ar command) {:ok? false :result command}
   (if-not (extends? Validatable (type command))
     {:ok? true :result command}
     (let [errors (first (p/validate ^Validatable command))]
       (if (nil? errors)
         {:ok? true :result command}
         {:ok? false :result errors})))))

(defn- process-command
  "handle the command , meanwhile store
   and emit the events produced by command to their channel "
  [command channel-map snapshot-db events-db readmodel id-creators recoverable-id-db]
  (let [ar (get-ar (:ar command) (:ar-id command) snapshot-db)]
    (let [handle-result (p/handle-command command ar)
          old-snapshot (first handle-result)
          new-events (rest handle-result)
          new-events-with-id
          (map #(assoc % :event-id
                       (inc-id-for :event id-creators recoverable-id-db))
               new-events)
          new-snapshot (get-ar handle-result)]
      (es/store-snapshot-and-events new-snapshot
                                    new-events-with-id
                                    snapshot-db
                                    events-db)
      (dorun (map #(prepare-and-emit-event channel-map readmodel % {})
                  new-events-with-id)))))


(defn replay-events
  [store channel-map readmodel]
  (let []
    (log/info "[=>]replaying events to rebuild the state of entries")
    (storage/map
     store
     (fn [k v]
       (prepare-and-emit-event
        channel-map
        readmodel
        (convert/->data v) {})))
    (log/info "[<=]replayed events")))

(defn fetch
  "fetch result of query"
  [query]
  (if (:id query)
    (p/find-by-id query)
    (p/query query)))


(defrecord SimpleCommandBus [channel-map readmodel snapshot-db events-db id-creators recoverable-id-db]
  p/CommandBus
  (sends [this command options]
    (let [validated-command (validate-command command)]
      (if-not (:ok? validated-command) validated-command
              (let [command-with-id (populate-command-id-if-need
                                     command id-creators recoverable-id-db)]
                (emit-command
                 channel-map
                 (fn [cmd]
                   (process-command cmd channel-map
                                    snapshot-db events-db readmodel
                                    id-creators recoverable-id-db))
                 command-with-id
                 options)
                (:ar-id command-with-id))))))
