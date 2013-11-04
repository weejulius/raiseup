(ns cqrs.hazelcast.readmodel
  (:require [common.cache :as c]
            [cqrs.protocol :as cqrs]
            [clojure.core.reducers :as r]
            [common.convert :as convert])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))

(defn get-read-caches
  []
  (c/get-cache :readmodel-cache (fn [] (Hazelcast/newHazelcastInstance nil))))

(defn load-entries
  "get the read models from cache by model name"
  [caches entry-type]
  (.getMap caches entry-type))

(defn conj+
  "conj vararier for fold function"
  ([a b]
     (conj a b))
  ([]
     []))


(defrecord HazelcastReadModel [caches]
  cqrs/ReadModel
  (load-entry [type id]
    (let [entries (load-entries caches type)
          entry (.get entries id)]
      entry))
  (put-entry [entry-type new-entry]
    (.put (load-entries caches type) (:ar-id new-entry) new-entry))
  (update-entry [entry-type id f]
    (let [entry (load-entry type id)
          new-entry (f entry)]
      (put-entry entry-type new-entry)))
  (remove-entry [type id]
    (let [entries (load-entries type id)]
      (.remove entries id)))
  (query [type f]
    (r/fold conj+ (r/filter f
                            (r/map identity (.values (load-entries type)))))))
