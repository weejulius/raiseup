(ns env
  (:use [org.httpkit.server :only [run-server]])
  (:require [cqrs.core :as cqrs]
            [common.component :as component]
            [common.logging :as log]
            [ring.middleware.reload :as reload]
            [ring.middleware.gzip :as gzip]
            [compojure.handler :refer [site]]
            [common.logging :as log]
            [cqrs.elastic-rm :as rm]
            [cqrs.storage :as storage])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))

(defn- start-http-server
  [port-str ip routes]
  (let [handler (site routes)
        wrapped-handler (gzip/wrap-gzip
                         (reload/wrap-reload handler))
        port (Integer/parseInt port-str)
        stop-fn  (run-server wrapped-handler {:port port :ip ip})]
    (log/info (str "Server started at " ip ":" port-str))
    stop-fn))


(defrecord NoteSystem []
  component/Lifecycle
  (init [this options]
    (let [opened-dbs (atom {})
          recoverable-id-db (storage/init-store opened-dbs
                                                (:id-db-path options)
                                                (:leveldb-option options))
          new-state
          (-> this
              (assoc :opened-dbs opened-dbs)
              (assoc :snapshot-db
                (storage/init-store opened-dbs
                                    (:snapshot-db-path options)
                                    (:leveldb-option options)))
              (assoc :channels (atom {}))
              (assoc :recoverable-id-db recoverable-id-db)
              (assoc :events-db
                (storage/init-store opened-dbs
                                    (:events-db-path options)
                                    (:leveldb-option options)))
              (assoc :id-creators (atom {}))
              (assoc :readmodel (.init
                                 (rm/->ElasticReadModel (:app (:elastic options)))
                                 (:elastic options))))]
      (assoc new-state :command-bus  (cqrs/->SimpleCommandBus
                                      (:channels new-state)
                                      (:readmodel new-state)
                                      (:snapshot-db new-state)
                                      (:events-db new-state)
                                      (:id-creators new-state)
                                      (:recoverable-id-db new-state)))))
  (start [this options]
    (let [updated (assoc this
                    :http-server
                    (start-http-server
                     (:port options)
                     (:host options)
                     (:routes options)))]
      (cqrs/replay-events (:events-db updated)
                          (:channels updated)
                          (:readmodel updated))
      updated))
  (stop [this options]
    (do
      (if-not (nil? (:opened-dbs this))
          (let [dbs @(:opened-dbs this)]
            (log/debug "dbs " dbs)
            (if-not (empty? dbs)
              (try
                (doseq [[key ^org.iq80.leveldb.DB db] dbs]
                  (do
                    (log/info "shutdowning db" db)
                    (.close db)))
                (catch Exception e
                  (log/error e))))))
      (if-let [stop-http (:http-server this)]
        (stop-http :timeout 1))
      (assoc this :readmodel nil))))
