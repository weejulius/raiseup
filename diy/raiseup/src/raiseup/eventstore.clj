(ns raiseup.eventstore
  (:require [raiseup.mutable :as default]
            [raiseup.storage :as store]
            [raiseup.base :as base]))

(defn make-store-key-of-event-ids
  "used to make the key for the event ids in the storage,
   event-ids is used to locate the events for ar quickly"
  [ar-name-str ar-id-str]
  (str ar-name-str ar-id-str))

(defn store-events-id-mapped-by-ar-id
  "map the events for ar by the ar's id
   in order to find the events for ar quickly"
  [ar-name-str ar-id-str event-ids db]
  (let [store-key (make-store-key-of-event-ids ar-name-str ar-id-str)
        event-ids-byte (store/find-value-by-key store-key db)
        current-eventids (base/byte-to-int-array event-ids-byte)
        appended-eventids (distinct (into current-eventids event-ids))
        batch-process (.createWriteBatch db)]
    (.put batch-process
          (base/to-bytes store-key)
          (base/int-to-bytes appended-eventids))
    (.write db batch-process)))

(defn serialize
  [event]
  (base/data->bytes event))

(defn deserialize
  [bytes]
  (base/bytes->data bytes))


(defn write-events-to-leveldb
  [events events-db]
  (let [batch  (.createWriteBatch events-db)]
    (doseq [event events]
      (.put batch
            (base/int-to-bytes [(:event-id event)])
            (serialize event)))
    (.write events-db batch)))

(defn store-events
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc "store the umcommitted events of aggregate root"}
  [ar-name ar-id new-events ar-eventids-db events-db]
  (let [ar-name-str (name ar-name)
        ar-id-str (str ar-id)
        new-event-ids (map #(:event-id %) new-events)]
    (future (write-events-to-leveldb) new-events events-db)
    (store-events-id-mapped-by-ar-id
       ar-name-str
       ar-id-str
       new-event-ids
       ar-eventids-db)))

(defn read-event-ids
  [ar-name-str ar-id-str ar-event-ids-db]
  (if-let [event-ids-byte
        (store/find-value-by-key
         (make-store-key-of-event-ids ar-name-str ar-id-str)
         ar-event-ids-db)]
      (base/byte-to-int-array event-ids-byte)))

(defn read-event
  [event-id-byte events-db]
  (if-let [event-byte (.get events-db event-id-byte)]
     (deserialize event-byte)))


(defn read-events
  [ar-name ar-id ar-event-ids-db events-db]
  (let [event-ids (read-event-ids (name ar-name) (str ar-id) ar-event-ids-db)]
    (map
     (fn [event-id]
      (read-event (base/int-to-bytes [event-id]) events-db))
     event-ids)))
