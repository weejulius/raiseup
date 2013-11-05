(ns ontime
  (:require [cqrs.hazelcast.readmodel :as readmodel]))

(def models
  "init and hold the hazelcast read model"
  (readmodel/->HazelcastReadModel (readmodel/get-read-caches)))