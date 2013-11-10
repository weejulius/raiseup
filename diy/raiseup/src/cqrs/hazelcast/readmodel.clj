(ns cqrs.hazelcast.readmodel
  (:require [cqrs.protocol :as cqrs]
            [common.logging :as log]
            [clojure.core.reducers :as r]
            [common.convert :as convert]))


(defn load-entries
  "get the read models from cache by model name"
  [caches entry-type]
  (.getMap caches (name entry-type)))

(defn conj+
  "extend conj fun and able to deal with the empty vec"
  ([a b]
     (conj a b))
  ([]
     []))

(defrecord HazelcastReadModel [caches]
  cqrs/ReadModel
  (load-entry [this type id]
    "load single entry by id"
    (let [entries (load-entries caches type)
          entry (.get entries id)]
      entry))

  (put-entry [this new-entry]
    "create or update entry"
    (.put (load-entries caches (:ar new-entry))
          (:ar-id new-entry)
          new-entry))

  (update-entry [this entry-type id f]
    "apply f to the specific entry and update"
    (let [entry (.load-entry this entry-type id)
          new-entry (f entry)]
      (.put-entry this new-entry)))

  (remove-entry [this type id]
    (let [entries (load-entries caches type)]
      (.remove entries id)))

  (do-query [this type f]
    "filter each entry and combine the qualified ones"
    (r/fold conj+ (r/filter
                   f
                   (r/map identity
                          (.values (load-entries caches type)))))))
