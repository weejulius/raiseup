(ns system
  (:require [common.logging :as log]
            [cqrs.hazelcast.readmodel :as rm]
            [cqrs.storage :as storage]
            [common.config :as cfg])
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

(defonce opened-dbs (atom {}))
(defonce recoverable-id-db (storage/init-store opened-dbs
                                          (cfg/ret :es :id-db-path)
                                          (cfg/ret :leveldb-option)))
(defonce system
  {:readmodel (rm/->HazelcastReadModel
               (Hazelcast/newHazelcastInstance nil))
   :opened-dbs opened-dbs
   :snapshot-db (storage/init-store opened-dbs
                                  (cfg/ret :es :snapshot-db-path)
                                  (cfg/ret :leveldb-option))
   :channels (atom {})
   :recoverable-id-db recoverable-id-db
   :events-db (storage/init-store opened-dbs
                                  (cfg/ret :es :events-db-path)
                                  (cfg/ret :leveldb-option))
   :id-creators (atom {})})
(reset! (:channels system) {})
(def entries (:readmodel system))
