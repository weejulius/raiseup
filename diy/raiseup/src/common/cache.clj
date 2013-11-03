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
  ([cache-kw keys cached-item]
     (let [cache-name (str cache-kw)
           cache (@caches cache-name)
           path (vec (cons  cache-name keys))]
       (if (nil? cache)
         (let []
           (log/debug "cache new item" cache-kw keys cached-item)
           (swap! caches
                  (fn [c] (assoc-in c path (cached-item))))))
       (get-in @caches path))))
