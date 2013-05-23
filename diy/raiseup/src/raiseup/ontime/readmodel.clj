(ns raiseup.ontime.readmodel
  (:require [raiseup.mutable-data :as md]
            [raiseup.base :as base]))

(defn get-readmodel
  "get the read model from cache by the model name and its model id"
  ([model-name k cache]
     (let [readmodels (.getMap cache (name model-name))
           readmodel (.get readmodels k)]
       (println "key:" k " -> " readmodel " :all " readmodels)
       readmodel))
  ([model-name k]
     (get-readmodel model-name k (md/readmodel-cache))))

(defn put-in-readmodel
  "put the read model inside the cache"
  ([model-name k v cache]
     (let [readmodel (.getMap cache (name model-name))]
       (.put readmodel k v)))
  ([model-name k v]
     (put-in-readmodel model-name k v (md/readmodel-cache))))

(defn update-in-readmodel
  "update the read model inside the cache"
  [model-name key f]
  (let [v (get-readmodel model-name key)
        v1 (f v)]
    (put-in-readmodel model-name key v1)))

(defn task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (put-in-readmodel (:ar event) ar-id event)
    (update-in-readmodel
     :user-slots
     (:user-id event)
     (fn
       [slots]
       (update-in
        slots
        [(if (empty? start-time):none start-time)]
        conj ar-id) ))))
