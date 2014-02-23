(ns cqrs.eventstore
  (:require [common.convert :refer [->data ->bytes]]
            [common.logging :as log]
            [cqrs.storage :as store]))

;;deprecate
(defn- make-store-key-of-event-ids
  "used to make the key for the event ids in the storage,
   each ar ownes a couple of events, therefore event-ids is
   used to locate the events in the event store for ar quickly"
  [ar-name-str ar-id-str]
  (str ar-name-str ar-id-str))

;;deprecate
(defn store-events-id-mapped-by-ar-id
  "store the event id of ar'events so as to find the events for ar quickly"
  [ar-name-str ar-id-str event-ids storage]
  (let [store-key (make-store-key-of-event-ids ar-name-str ar-id-str)
        event-ids-byte (store/ret-value storage store-key)
        current-eventids (->data event-ids-byte)
        appended-eventids (distinct (into current-eventids event-ids))]
    (store/write storage
                 store-key
                 appended-eventids)))


(defn write-events-to-storage
  "the events are stored in the storage and the event id is the store key"
  [events events-db]
  (store/write-in-batch events-db
                        (map (fn [event] [(:event-id event) event]) events)))

(defn store-snapshot
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc   "store the snapshot"}
  [snapshot snapshot-db]
  (let [snapshot-key (str (:ar snapshot) (:ar-id snapshot))
        cur-snapshot (->data (store/ret-value snapshot-db snapshot-key))]
    (if-not (nil? cur-snapshot)
      (cond
        (nil? (:ar-id snapshot))
        (throw (ex-info "ar-id is missing of snapshot"
                        {:snapshot snapshot}))
        (not (= 1 (- (:vsn snapshot) (:vsn cur-snapshot))))
        (throw (ex-info "version conflict storing snapshot"
                        {:cur-vsn (:vsn cur-snapshot)
                         :new-vsn (:vsn snapshot)
                         :cur     cur-snapshot
                         :new     snapshot}))))
    (log/debug "store snapshot" snapshot-key snapshot)
    (store/write snapshot-db
                 snapshot-key
                 snapshot)))

(defn retreive-ar-snapshot
  [ar-name ar-id snapshot-db]
  (if-let [snapshot-bytes (store/ret-value snapshot-db (str ar-name ar-id))]
    (->data snapshot-bytes)))
;;deprecate
(defn read-event-ids
  "the events ids are required when fetching an aggregate root
   then reading the events for ar according the read event ids"
  [ar-name-str ar-id-str ar-event-ids-db]
  (if-let [event-ids-byte
           (store/ret-value ar-event-ids-db
                            (make-store-key-of-event-ids ar-name-str ar-id-str))]
    (->data event-ids-byte)))

(defn read-event
  "read a single event by event id from event store"
  [event-id events-db]
  (if-let [event-byte (store/ret-value events-db (str event-id))]
    (->data event-byte)))

;;deprecate
(defn read-events
  "used to read events for an ar, firstly read its event ids from storage,
   and then read the actual events by the read event ids"
  [ar-name ar-id ar-event-ids-db events-db]
  (let [event-ids (read-event-ids
                    (name ar-name)
                    (str ar-id)
                    ar-event-ids-db)]
    (map
      (fn [event-id]
        (read-event event-id events-db))
      event-ids)))
