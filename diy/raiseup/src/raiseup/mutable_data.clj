(ns raiseup.mutable-data
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))

(def caches
  "the caches" (atom {}))

(defn get-cache
  "get cached item from cache"
  [cache-kw cached-item]
  (let [cache-name (name cache-kw)
        cache (@caches cache-name)]
    (println "getting cache " cache)
    (if (nil? cache)
      ((swap! caches (fn [c]
                        (assoc c cache-name
                               (cached-item))))
       cache-name)
      cache)))

(defn readmodel-cache
  []
  (get-cache :readmodel-cache (fn [] (Hazelcast/newHazelcastInstance (Config.)))))
