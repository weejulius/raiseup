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


(defn ->send
  "send command to bus"
  [command command-router event-router]
  (let [cmd-fun ((:command command) command-router)
        produced-events (cmd-fun command)
        events-with-id (map
                        #(assoc % :event-id (.inc! event-id-creator))
                        [produced-events])]
    (println produced-events events-with-id)
    (es/store-events (:ar command)
                     (:ar-id command)
                     events-with-id
                     event-ids-db
                     events-db)
    (map #(eventbus/->send %  event-router) events-with-id)))

(defn <-read
  [ar-name ar-id]
  (es/read-events ar-name ar-id event-ids-db events-db))
