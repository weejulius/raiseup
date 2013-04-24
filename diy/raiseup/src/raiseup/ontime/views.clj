(ns raiseup.ontime.views
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))

(def cache (Hazelcast/newHazelcastInstance (Config.)))

(defn get
  ([name key cache]
     (.get (.getMap cache (name name)) key))
  ([name key]
     (get name key cache)))

(defn put
  ([name key value cache]
     (.put (.getMap cache (name name)) key value))
  ([name key value]
     (put name key value cache)))

(defn task-slot-created
  [event]
  (put (:ar event) (:ar-id event) event))
