(ns cqrs.storage
  (:require [common.convert :as convert]
            [cqrs.leveldb :as leveldb]))

(def flush-recoverable-id-interval 100000)

(defprotocol Store
  "the protocol to utilize the store"
  (ret-value [this key] "return value by key in the store")
  (write [this key-bytes value-bytes] "store the key value which is bytes")
  (write-in-batch [this items] "store items in batch")
  (delete [this key] "delete the value by key"))

(defn serialize
  "serialize the clojure data structure to bytes"
  [event]
  (convert/data->bytes event))

(defrecord LeveldbStore
    [db]
  Store
  (ret-value [this key]
    (.get db (convert/to-bytes key)))
  (write [this key value]
    (.put db key value))
  (write-in-batch [this items]
    (let [batch  (.createWriteBatch db)]
      (doseq [item items]
        (.put batch
              (convert/long->bytes (first item))
              (serialize (second item))))
      (.write db batch)))
  (delete [this key]
    (.delete db (convert/to-bytes key))))

(defprotocol RecoverableId
  "an uniqure identifier,it can be recoved after restart,
   but it does not garantee the identifier is sequential, the identifier
   will add the defined incremence whenever recovery from scrash"
  (init! [this] "init the id")
  (get! [this] "return the current value of id")
  (inc! [this] "increase the id")
  (clear! [this] "clear and reset the state of id"))



(defrecord RecoverableLongId
    [^String store-key storage long-id incremence]
  RecoverableId
  (init! [this]
    (let [cur-value (convert/->long (.ret-value storage store-key))
          new-value (if (nil? cur-value) (long 0)
                        (+ cur-value incremence))]
      (reset! long-id new-value)
      (.write storage
              (convert/to-bytes store-key)
              (convert/long->bytes new-value))))
  (get! [this]
    @long-id)
  (inc! [this]
    (let [inc-value (swap! long-id inc)]
      (if (= 0 (mod inc-value incremence))
        (.write
         storage
         (convert/to-bytes store-key)
         (convert/long->bytes (long inc-value))))
      inc-value))
  (clear! [this]
    (.delete storage store-key)
    (reset! long-id 0)))

(defn init-recoverable-long-id
  "factory of recoverable long id"
  [name storage]
  (let [recoverable (->RecoverableLongId name storage (atom -1)
                                         flush-recoverable-id-interval)]
    (.init! recoverable)
    recoverable))



(defn init-store
  "factory of storage"
  [dir options]
  (->LeveldbStore (leveldb/open-leveldb dir options)))
