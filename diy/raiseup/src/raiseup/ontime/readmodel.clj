(ns raiseup.ontime.readmodel
  (:require [raiseup.mutable-data :as md]))

(defn get-readmodel
  "get the read model from cache by the model name and its model id"
  ([model-name k cache]
     (let [readmodels (.getMap cache (name model-name))
           readmodel (.get readmodels k)]
       readmodel))
  ([model-name k]
     (get-readmodel model-name k (md/readmodel-cache))))

(defn put-in-readmodel
  "put item into the  read model inside the cache"
  ([model-name k v cache]
     (let [readmodel (.getMap cache (name model-name))]
       (.put readmodel k v)))
  ([model-name k v]
     (put-in-readmodel model-name k v (md/readmodel-cache))))

(defn task-slot-created
  [event]
  (println "creating task slot " event)
  (put-in-readmodel (:ar event) (:ar-id event) event)
  (put-in-readmodel :user-slots (:user-id event) conj event))
