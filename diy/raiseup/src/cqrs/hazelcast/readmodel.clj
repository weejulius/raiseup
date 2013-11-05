(ns cqrs.hazelcast.readmodel
  (:require [common.cache :as c]
            [cqrs.protocol :as cqrs]
            [clojure.core.reducers :as r]
            [common.convert :as convert])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))


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


(defrecord ^{:doc "use hazelcase as the read model cache"
             :added "1.0"}
    HazelcastReadModel [caches]
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

    (query [this type f]
      "filter each entry and combine the qualified ones"
      (r/fold conj+ (r/filter
                     f
                     (r/map identity (.values (load-entries caches type)))))))
