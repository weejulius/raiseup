(ns cqrs.storage
  (:refer-clojure :exclude [map])
  (:require [common.convert :refer [->bytes ->long ->data]]
            [cqrs.leveldb :as leveldb]
            [common.config :as cfg]
            [common.logging :as log])
  (:import (org.iq80.leveldb DB DBIterator WriteBatch)
           (java.util Map$Entry)
           (clojure.lang Atom))
  (:gen-class))

(def flush-recoverable-id-interval (cfg/ret :recoverable-id-flush-interval))

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
  (write [this  key  value]
    (.put db key value))
  (write-in-batch [this items]
    (let [^WriteBatch batch  (.createWriteBatch db)]
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
  "an uniqure identifier,it can be recoved after restart,
   but it does not garantee the identifier is sequential, the identifier
   will add the defined incremence whenever recovery from scrash"
  (init! [this] "init the id")
  (get! [this] "return the current value of id")
  (inc! [this] "increase the id")
  (clear! [this] "clear and reset the state of id"))

(defrecord RecoverableLongId
    [^String store-key storage ^Atom long-id ^long incremence]
  RecoverableId
  (init! [this]
    (let [cur-value (->long (ret-value storage store-key))
          new-value (if (nil? cur-value) (long 0)
                        (+ cur-value incremence))]
      (reset! long-id new-value)
      (write storage
              (->bytes store-key)
              (->bytes new-value))
      this))
  (get! [this]
    @long-id)
  (inc! [this]
    (let [inc-value (swap! long-id inc)]
      (if (zero? (mod inc-value incremence))
        (write
         storage
         (->bytes store-key)
         (->bytes (long inc-value))))
      inc-value))
  (clear! [this]
    (doto (delete storage store-key)
      (reset! long-id 0))))

(defn init-recoverable-long-id
  "factory of recoverable long id"
  [name storage]
  (let [recoverable (->RecoverableLongId name storage (atom -1)
                                         flush-recoverable-id-interval)]
    (log/debug "init the recoverable long id" name)
    (init! recoverable)
    recoverable))



(defn init-store
  "factory of storage"
  [opened-dbs dir options]
  (->LeveldbStore (leveldb/open-leveldb! opened-dbs dir options)))
