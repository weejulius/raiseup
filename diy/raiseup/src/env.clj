(ns env
  "the side effect of application stay here"
  (:use [org.httpkit.server :only [run-server]])
  (:require [cqrs.core :as cqrs]
            [common.component :as component]
            [common.logging :as log]
            [ring.middleware.reload :as reload]
            [ring.middleware.gzip :as gzip]
            [compojure.handler :refer [site]]
            [common.logging :as log]
            [cqrs.elastic-rm :as rm]
            [cqrs.storage :as storage]
            [cqrs.vertx :refer :all])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)
           (cqrs.vertx VertxBus)))

(defn- start-http-server
  [port-str ip routes]
  )

;; the side effect for note
(defrecord NoteSystem []
  component/Lifecycle
  (init [this options]
    )
  (start [this options]

    (assoc this
      :http-server
      (start-http-server
        (:port options)
        (:host options)
        (:routes options))))
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


(defonce system (NoteSystem.))

(defonce bus (component/init (VertxBus.) nil))
