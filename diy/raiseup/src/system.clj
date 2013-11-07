(ns system
  (:require [common.logging :as log]
            [cqrs.hazelcast.readmodel :refer :all]
             [cqrs.storage :as storage])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))


;; (extend-protocol Lifecycle
;;   HazelcastReadModel
;;   (start [component]
;;     (if (.isRunning (.getLifecycleService (:caches component)))
;;       (do (log/info "hazelcast is running already.")
;;           component)
;;       (let [caches (Hazelcast/newHazelcastInstance nil)]
;;         (log/info "starting hazelcast instance as read model")
;;         (assoc component :caches caches))))
;;   (stop [component]
;;     (if (.isRunning (.getLifecycleService (:caches component)))
;;       (do (log/info "shutting down hazelcast")
;;           (.shutdown (:caches component))
;;           (assoc component :caches nil)))
;;     component))
(defonce system
  {:readmodel (->HazelcastReadModel
               (Hazelcast/newHazelcastInstance nil))
   :channels {}
   :event-id-db (storage/init-store (cfg/ret :es :event-id-db-path)
                                    (cfg/ret :leveldb-option))
   :events-db (storage/init-store (cfg/ret :es :events-db-path)
                                  (cfg/ret :leveldb-option))
   :event-id-creator (storage/init-recoverable-long-id
                      (cfg/ret :es :recoverable-event-id-key)
                      event-ids-db)
   :ar-id-creator (storage/init-recoverable-long-id
                   (cfg/ret :es :recoverable-ar-id-key)
                   event-id-db)
   })

(def entries (:readmodel system))
