(ns env
  (:use [org.httpkit.server :only [run-server]])
  (:require [cqrs.core :as cqrs]
            [common.component :as component]
            [common.logging :as log]
            [ring.middleware.reload :as reload]
            [compojure.handler :refer [site]]
            [common.logging :as log]
            [cqrs.elastic-rm :as rm]
            [cqrs.storage :as storage]
            [common.config :as cfg])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))

(defn- start-http-server
  [port-str ip routes]
  (let [handler (site routes) ;;
        port (Integer/parseInt port-str)
        stop-fn  (run-server handler {:port port :ip ip})]
    (log/info (str "Server started at " ip ":" port-str))
    stop-fn))


(defrecord NoteSystem []
  component/Lifecycle
  (init [this options]
    (let [opened-dbs (atom {})
          recoverable-id-db (storage/init-store opened-dbs
                                                (cfg/ret :es :id-db-path)
                                                (cfg/ret :leveldb-option))
          new-state
          (-> this
              (assoc :opened-dbs opened-dbs)
              (assoc :snapshot-db
                (storage/init-store opened-dbs
                                    (cfg/ret :es :snapshot-db-path)
                                    (cfg/ret :leveldb-option)))
              (assoc :channels (atom {}))
              (assoc :recoverable-id-db recoverable-id-db)
              (assoc :events-db
                (storage/init-store opened-dbs
                                    (cfg/ret :es :events-db-path)
                                    (cfg/ret :leveldb-option)))
              (assoc :id-creators (atom {}))
              (assoc :readmodel (-> (rm/->ElasticReadModel (cfg/ret :elastic :app))
                                    (.init (cfg/ret :elastic)))))]
      (assoc new-state :command-bus  (cqrs/->SimpleCommandBus
                                      (:channels new-state)
                                      (:snapshot-db new-state)
                                      (:events-db new-state)
                                      (:id-creators new-state)
                                      (:recoverable-id-db new-state)))))
  (start [this options]
    (let [s (-> this
                (assoc :http-server
                  (start-http-server
                   (:port options)
                   (:host options)
                   (:routes options))))]
      (cqrs/replay-events (:events-db this) (:channels this))
      s))
  (stop [this options]
    (do
      (if-let [dbs (:opened-dbs this)]
        (log/debug "dbs " (:opened-dbs this))
        (doseq  [[key db] @(:opened-dbs this)]
          (do
            (log/info "shutdowning db" db)
            (.close db))))
      (if-let [stop-http (:http-server this)]
        (stop-http :timeout 1))
      (assoc this :readmodel nil))))
