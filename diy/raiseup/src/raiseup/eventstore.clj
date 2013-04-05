(ns raiseup.eventstore
  (:require [raiseup.mutable :as default]
            [raiseup.storage :as store]
            [raiseup.base :as base]))

(defn make-store-key-of-event-ids
  [ar-name-str ar-id-str]
  (str ar-name-str ar-id-str))

(defn store-events-id-mapped-by-ar-id
  [ar-name-str ar-id-str event-ids db]
  (let [store-key (make-store-key-of-event-ids ar-name-str ar-id-str)
        event-ids-byte (store/find-value-by-key store-key db)
        current-eventids (base/byte-to-int-array event-ids-byte)
        appended-eventids (distinct (concat current-eventids event-ids))
        batch-process (.createWriteBatch db)]
    (.put batch-process
          (base/to-bytes store-key)
          (base/int-to-bytes appended-eventids))
    (.write db batch-process)))

(defn serialize
  [event]
  (base/json-to-str event default/eventstore-json-options))

(defn deserialize
  [str]
  (base/str-to-json str))


(defn write-events-to-leveldb
  [events events-db]
  (let [batch  (.createWriteBatch events-db)]
    (doseq [event events]
      (.put batch
            (base/int-to-bytes [(:event-id event)])
            (base/to-bytes (serialize event))))
    (.write events-db batch)))

(defn store-events
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc "store the umcommitted events of aggregate root"}
  [ar-name ar-id new-events ar-eventids-db events-db]
  (let [ar-name-str (name ar-name)
        ar-id-str (str ar-id)
        new-event-ids (map #(:event-id %) new-events)]
    (store-events-id-mapped-by-ar-id
     ar-name-str
     ar-id-str
     new-event-ids
     ar-eventids-db)
    (write-events-to-leveldb new-events events-db)))

(defn read-event-ids
  [ar-name-str ar-id-str ar-event-ids-db]
  (let [event-ids-byte
        (store/find-value-by-key
         (make-store-key-of-event-ids ar-name-str ar-id-str)
         ar-event-ids-db)]
    (if (nil? event-ids-byte)
      nil
      (base/byte-to-int-array event-ids-byte))))

(defn read-event
  [event-id-byte events-db]
  (let [event-byte (.get events-db event-id-byte)]
    (if (nil? event-byte)
      nil
      (deserialize (String. event-byte default/charset)))))


(defn read-events
  [ar-name ar-id ar-event-ids-db events-db]
  (let [event-ids (read-event-ids (name ar-name) (str ar-id) ar-event-ids-db)]
    (map
     (fn [event-id]
      (read-event (base/int-to-bytes [event-id]) events-db))
     event-ids)))
