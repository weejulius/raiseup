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
           old-cache  (get-in @caches path)]
       (if-not (nil? old-cache) old-cache
               (let [new-cache (new-cache-fn)]
                 (if-not (nil? new-cache)
                   (let []
                     (log/debug "cache new item" cache-kw keys new-cache)
                     (swap! caches
                            (fn [c] (assoc-in c path new-cache)))))
                 (get-in @caches path))))))
