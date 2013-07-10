(ns raiseup.ontime.readmodel
  (:require [raiseup.mutable-data :as md]
            [raiseup.base :as base]))

(defn get-readmodels
  "get the read models from cache by model name"
  ([cache model-name]
      (.getMap cache (name model-name)))
  ([model-name]
     (get-readmodels (md/readmodel-cache) model-name)))

(defn get-readmodel
  "get the read model from cache by the model name and its model id"
  ([model-name k cache]
     (let [readmodels (get-readmodels cache model-name)
           readmodel (.get readmodels k)]
       (println  model-name "key:" k " -> " readmodel " :all " readmodels)
       readmodel))
  ([model-name k]
     (get-readmodel model-name k (md/readmodel-cache))))

(defn put-in-readmodel
  "put the read model inside the cache"
  ([model-name k v cache]
     (let [readmodel (get-readmodels cache model-name)]
       (.put readmodel k v)))
  ([model-name k v]
     (put-in-readmodel model-name k v (md/readmodel-cache))))
(defn remove-from-readmodel
  ([model-name k cache]
     (let [readmodels (get-readmodels cache model-name)]
       (.remove readmodels k)))
  ([model-name k]
     (remove-from-readmodel model-name k (md/readmodel-cache))))

(defn update-in-readmodel
  "update the read model inside the cache"
  [model-name key f]
  (let [v (get-readmodel model-name key)
        v1 (f v)]
    (put-in-readmodel model-name key v1)))

(defn query
  "query the cache"
  [model-name query-clause]
  (.values (get-readmodels model-name)
           (com.hazelcast.query.SqlPredicate. query-clause)))

(defn task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (put-in-readmodel (:ar event) ar-id event)))

(defn task-slot-deleted
  [event]
  (println "^task-slot-deleted " event)
  (let [ar-id (base/->long (:ar-id event))]
    (remove-from-readmodel (:ar event) ar-id)))
