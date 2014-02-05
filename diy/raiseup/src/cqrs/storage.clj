(ns cqrs.storage
  (:refer-clojure :exclude [map])
  (:require [common.convert :refer [->bytes ->long ->data]]
            [cqrs.leveldb :as leveldb]
            [common.config :as cfg]
            [common.logging :as log])
  (:import (org.iq80.leveldb DB DBIterator WriteBatch)
           (java.util Map$Entry Map)
           (clojure.lang Atom IPersistentMap)
           (java.util.concurrent.atomic AtomicLong))
  (:gen-class))

(defprotocol Store
  "the protocol to utilize the store"
  (ret-value [this key] "return value by key in the store")
  (write [this key-bytes value-bytes] "store the key value which is bytes")
  (write-in-batch [this items] "store items in batch")
  (delete [this key] "delete the value by key")
  (map [this f] "apply f for each item")
  (close [this] "close the storage and call f"))


(defrecord LeveldbStore
           [^DB db]
  Store
  (ret-value [this key]
    (.get db (->bytes key)))
  (write [this key value]
    (.put db key value))
  (write-in-batch [this items]
    (let [^WriteBatch batch (.createWriteBatch db)]
      (doseq [item items]
        (.put batch
              (->bytes (first item))
              (->bytes (second item))))
      (.write db batch)))
  (delete [this key]
    (.delete db (->bytes key)))
  (map [this f]
    (let [^DBIterator iterator (.iterator db)]
      (.seekToFirst iterator)
      (clojure.core/map #(apply f %) (iterator-seq iterator))))
  (close [this]
    (.close db)))

(defprotocol RecoverableId
  "uniqure identifiers which can be recoved after restart,
   but it does not garantee the identifier is sequential, the identifier
   will add the defined incremence whenever recovery from down"
  (get! [this id-name] "return the current value of id")
  (inc! [this id-name] "increase the id")
  (clear! [this id-name] "clear and reset the state of id"))

(defn- ^AtomicLong get-or-init-recoverable-id-if-need
  "get id and init the recoverable id if not initialized"
  [storage id-name long-ids flush-recoverable-id-interval]
  (let [long-id (get @long-ids id-name)]
    (if (nil? long-id)
      (let [cur-value (->long (ret-value storage id-name))
            new-value (if (nil? cur-value) (long 0)
                                           (+ cur-value flush-recoverable-id-interval))]
        (swap! long-ids #(assoc % id-name (AtomicLong. new-value)))
        (write storage
               (->bytes id-name)
               (->bytes new-value))
        (log/debug "init the recoverable long id for " id-name)))
    (println id-name long-ids)
    (get @long-ids id-name)))

(defrecord RecoverableLongId
           [storage ^Atom long-ids ^long flush-recoverable-id-interval]
  RecoverableId
  (get! [this id-name]
    (.get (get-or-init-recoverable-id-if-need storage id-name long-ids flush-recoverable-id-interval)))
  (inc! [this id-name]
    (let [inc-value (.incrementAndGet
                      (get-or-init-recoverable-id-if-need storage id-name long-ids flush-recoverable-id-interval))]
      (if (zero? (mod inc-value flush-recoverable-id-interval))
        (write
          storage
          (->bytes id-name)
          (->bytes (long inc-value))))
      inc-value))
  (clear! [this id-name]
    (doto (delete storage id-name)
      (swap! long-ids #(assoc % id-name (AtomicLong.))))))

(defn init-recoverable-long-id
  "factory of recoverable long  id"
  [storage]
  (let [recoverable (->RecoverableLongId storage (atom {})
                                         (cfg/ret :recoverable-id-flush-interval))]
    recoverable))



(defn init-store
  "factory of storage"
  [opened-dbs dir options]
  (->LeveldbStore (leveldb/open-leveldb! opened-dbs dir options)))
