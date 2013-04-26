(ns raiseup.commandbus
  (:require [raiseup.eventbus :as eventbus]
            [raiseup.eventstore :as es]
            [raiseup.storage :as storage]
            [raiseup.mutable :as default]))

(def event-ids-db (storage/open-leveldb default/eventids-db-path
                                           default/default-leveldb-option))

(def events-db  (storage/open-leveldb default/events-db-path
                                           default/default-leveldb-option))

(def event-id-creator (storage/init-recoverable-long-id
                       default/event-identifier
                       event-ids-db))

(def ar-id-creator (storage/init-recoverable-long-id
                    default/ar-identifier
                    event-ids-db))

(defn- populate-id-if-need
  "if the id is not existing one is given"
  [command]
  (if (nil? (:ar-id command))
    (assoc command :ar-id (.inc! ar-id-creator)) command))


(defn ->send
  "send command to bus"
  [command command-router event-router]
  (let [cmd-fun ((:command command) command-router)
        command-with-id (populate-id-if-need command)
        produced-events (cmd-fun command-with-id)
        events-with-id (map
                        #(assoc % :event-id (.inc! event-id-creator))
                        [produced-events])]
    (println "processing command for ar " (:ar command-with-id) ":"  (:ar-id command-with-id))
    (es/store-events (:ar command-with-id)
                     (:ar-id command-with-id)
                     events-with-id
                     event-ids-db
                     events-db)
    (dorun (map #(eventbus/->send %  event-router) events-with-id))
    (:ar-id command-with-id)))

(defn <-read
  [ar-name ar-id]
  (es/read-events ar-name ar-id event-ids-db events-db))
