(ns ontime
  (:require [cqrs.hazelcast.readmodel :as readmodel]))

(def models (->readmodel/HazelcastReadModel (readmodel/get-read-models)))
