(ns raiseup.ontime.readmodel
  (:require [raiseup.mutable-data :as md]
            [common.convert :as convert]
            [clojure.core.reducers :as r]
            [cqrs.protocol :refer [on-event]]))

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

(defn conj+
  "conj vararier for fold function"
  ([a b]
     (conj a b))
  ([]
     []))

(defn query
  "query the cache"
  [model-name f]
  (r/fold conj+ (r/filter f
              (r/map identity (.values (get-readmodels model-name))))))

(defmethod on-event
  :task-slot-created
  [event]
  (let [ar-id (:ar-id event)
        start-time (:start-time event)]
    (put-in-readmodel (:ar event) ar-id event)))

(defmethod on-event
  :task-slot-deleted
  [event]
  (let [ar-id (convert/->long (:ar-id event))]
    (remove-from-readmodel (:ar event) ar-id)))

(defmethod on-event
  :task-slot-started
  [event]
  (update-in-readmodel (:ar event)
                       (:ar-id event)
                       #(assoc % :start-time (:start-time event))))
