(ns raiseup.ontime.views
  (:require [raiseup.mutable-data :as md]))

(defn get-view
  "get the item from cache by the view name and its key"
  ([view-name k cache]
     (let [view (.getMap cache (name view-name))
           item (.get view k)]
       (println "current view -> " k (type k)  item "-" view)
       (doseq [[k v] view]
         (println k (type k) ":" v))
       item))
  ([view-name k]
     (get-view view-name k (md/view-cache))))

(defn put-in-view
  "put item into the  view inside the cache"
  ([view-name k v cache]
     (let [view (.getMap cache (name view-name))]
       (.put view k v)
       (println "view after put -> "  (.get view k))))
  ([view-name k v]
     (put-in-view view-name k v (md/view-cache))))

(defn task-slot-created
  [event]
  (println "creating task slot " event)
  (put-in-view (:ar event) (:ar-id event) event))
