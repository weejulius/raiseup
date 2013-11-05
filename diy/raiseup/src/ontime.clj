(ns ontime
  (:require [cqrs.hazelcast.readmodel :as readmodel]
            [com.stuartsierra.component :as component]
            [common.logging :as log]))

(def entries
  "init and hold the hazelcast read model"
 )


(defrecord Cache [caches entries]
  component/LifeCycle
  (start [component]
    (if (.isRunning (.getLifecycleService caches))
      (do (log/info "hazelcast is running already.")
          component)
      (let [hazelcast-cache (Hazelcast/newHazelcastInstance nil)]
        (log/info "starting hazelcast instance as read model")
        (assoc (assoc component :caches hazelcast)
          :entries  (readmodel/->HazelcastReadModel hazelcast-cache)))))
  (stop [component]
    (if (.isRunning (.getLifecycleService caches))
      (do (log/info "shutting down hazelcast")
          (.shutdown caches)
          (assoc (assoc component :caches nil)
            :entries nil)))
    component))

def components [:caches :storage])
