(ns common.cache
  (:require [common.logging :as log]))

(def caches
  "the caches" (atom {}))

(defn get-cache
  "get cached item from cache"
  ([cache-kw cached-item]
     (let [cache-name (str cache-kw)
           cache (@caches cache-name)]
       (if (nil? cache)
         ((swap! caches (fn [c]
                          (assoc c cache-name
                                 (cached-item))))
          cache-name)
         cache)))
  ([cache-kw keys new-cache-fn]
     (let [cache-name (str cache-kw)
           path (vec (cons  cache-name keys))
           cache  (get-in @caches path)]
       (if-not (nil? cache) cache
               (let [new-cache (new-cache-fn)]
                 (log/debug "cache new item" cache-kw keys new-cache)
                 (if-not (nil? new-cache)
                   (get-in
                    (swap! caches
                           (fn [c] (assoc-in c path new-cache)))
                    path)
                   nil))))))
