(ns ontime
  (:require [cqrs.hazelcast.readmodel :as rm]))



(defn new-cache []
  (rm/map->HazelcastReadModel {}))
