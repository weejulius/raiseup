(ns cqrs.storage
  (:refer-clojure :exclude [map])
  (:require [common.convert :refer [->bytes ->long ->data]]
            [cqrs.leveldb :as leveldb]
            [common.component :as component]
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
  (write [this key value] "store the key value")
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
    (.put db (->bytes key) (->bytes value)))
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
    (.close db))

  component/Lifecycle
  (init [this options]
    this)
  (start [this options]
    (let [db (leveldb/open-leveldb!
               (:path options) (:leveldb-option options))]
      (assoc this :db db)))
  (stop [this options]
    (.close ^DB (:db this))
    this)
  (halt [this options]
    this))

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
        (write storage id-name new-value)
        (log/debug "init the recoverable long id for " id-name)))
    (get @long-ids id-name)))

(defrecord RecoverableLongId
           [storage]
  RecoverableId
  (get! [this id-name]
    (.get (get-or-init-recoverable-id-if-need
            storage id-name
            (:long-ids this) (:flush-recoverable-id-interval this))))
  (inc! [this id-name]
    (let [inc-value (.incrementAndGet
                      (get-or-init-recoverable-id-if-need
                        storage id-name
                        (:long-ids this) (:flush-recoverable-id-interval this)))]
      (if (zero? (mod inc-value (:flush-recoverable-id-interval this)))
        (write storage id-name (long inc-value)))
      inc-value))
  (clear! [this id-name]
    (doto (delete storage id-name)
      (swap! (:long-ids this) #(assoc % id-name (AtomicLong.)))))

  component/Lifecycle
  (init [this options]
    this)
  (start [this options]
    (-> this
        (assoc :storage (:storage options))
        (assoc :long-ids (atom {}))
        (assoc :flush-recoverable-id-interval (:recoverable-id-flush-interval options))))
  (stop [this options]
    this)
  (halt [this options]
    this))



