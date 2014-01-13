(ns cqrs.hazelcast.readmodel
  (:import (com.hazelcast.core HazelcastInstance IMap))
  (:require [cqrs.protocol :as cqrs]
            [cqrs.storage :as store]
            [common.logging :as log]
            [clojure.core.reducers :as r]
            [common.func :as func]
            [common.convert :as convert]))


(defn load-entries
  "get the read models from cache by model name"
  [^HazelcastInstance caches entry-type]
  (.getMap caches (name entry-type)))

(defn conj+
  "extend conj fun and able to deal with the empty vec"
  ([a b]
   (conj a b))
  ([]
   []))

(defrecord HazelcastReadModel [^HazelcastInstance caches]
  cqrs/ReadModel
  (load-entry [this type id]
    "load single entry by id"
    (let [^IMap entries (load-entries caches type)
          entry (.get entries id)]
      (log/debug "loading entry" entry type id)
      entry))

  (put-entry [this new-entry]
    "create or update entry"
    (let [^IMap entries (load-entries caches (:ar new-entry))]
      (log/debug "put new entry" new-entry)
      (.put entries
            (:ar-id new-entry)
            new-entry)))

  (update-entry [this entry-type id f]
    "apply f to the specific entry and update"
    (let [^IMap entry (cqrs/load-entry this entry-type id)
          new-entry (f entry)]
      (cqrs/put-entry this new-entry)))

  (remove-entry [this type id]
    (let [^IMap entries (load-entries caches type)]
      (.remove entries id)))

  (do-query [this type query]
    "filter each entry and combine the qualified ones"
    (func/filter-until
      (:each query)
      (:satisified query)
      (.values ^IMap (load-entries caches type)))))
