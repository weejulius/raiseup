(ns cqrs.eventstore
  (:require [common.convert :refer [->data ->bytes]]))

(defn- make-store-key-of-event-ids
  "used to make the key for the event ids in the storage,
   each ar ownes a couple of events, therefore event-ids is
   used to locate the events in the event store for ar quickly"
  [ar-name-str ar-id-str]
  (str ar-name-str ar-id-str))

(defn store-events-id-mapped-by-ar-id
  "store the event id of ar'events so as to find the events for ar quickly"
  [ar-name-str ar-id-str event-ids storage]
  (let [store-key (make-store-key-of-event-ids ar-name-str ar-id-str)
        event-ids-byte (.ret-value storage store-key)
        current-eventids (->data event-ids-byte)
        appended-eventids (distinct (into current-eventids event-ids))]
    (.write storage
            (->bytes store-key)
            (->bytes appended-eventids))))

(defn deserialize
  "deser the bytes to data structure"
  [bytes]
  (->data bytes))


(defn write-events-to-storage
  "the events are stored in the storage and the event id is the store key"
  [events events-db]
  (.write-in-batch events-db
                   (map (fn [event] [(:event-id event) event]) events)))

(defn store-events
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc "store the umcommitted events of aggregate root"}
  [ar-name ar-id new-events ar-eventids-db events-db]
  (let [ar-name-str (name ar-name)
        ar-id-str (str ar-id)]
    (write-events-to-storage new-events events-db)
    (store-events-id-mapped-by-ar-id ar-name-str
                                     ar-id-str
                                     (map #(:event-id %) new-events)
                                     ar-eventids-db)))


(defn read-event-ids
  "the events ids are required when fetching an aggregate root
   then reading the events for ar according the read event ids"
  [ar-name-str ar-id-str ar-event-ids-db]
  (if-let [event-ids-byte
           (.ret-value ar-event-ids-db
                       (make-store-key-of-event-ids ar-name-str ar-id-str))]
    (->data event-ids-byte)))

(defn read-event
  "read a single event by event id from event store"
  [event-id events-db]
  (if-let [event-byte (.ret-value events-db (str event-id))]
    (deserialize event-byte)))


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
