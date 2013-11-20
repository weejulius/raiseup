(ns main
  (:gen-class)
  (:use [org.httpkit.server :only [run-server]]
        ontime.handler)
  (:require [ring.middleware.reload :as reload]
            [compojure.handler :refer [site]]
            [common.config :as cfg]
            [cqrs.core :as cqrs]
            [system :as s]
            [common.logging :as log]))

(defn- start-http-server
  [port-str ip]
  (let [handler (site #'app-routes)
        port (Integer/parseInt port-str)]
    (run-server
     (if (cfg/dev-mode?)
       ;;auto load changes for dev mode
       (reload/wrap-reload handler)
       handler)
     {:port port :ip ip})))

(def http-server (atom nil))
(def has-replayed (atom false))

(defn -main
  [& args]
  (let [port (nth args 1 "8080")
        host (nth args 0 "localhost")
        stop-http-server-fn (start-http-server port host)]
    (log/info (str "Server started. listen at " host ":" port))
    (reset! http-server stop-http-server-fn)
    (when-not @has-replayed
      (cqrs/replay-events (:events-db s/system))
      (reset! has-replayed true))))


(defn stop
  "stop the server"
  []
  (@http-server :timeout 1))


(defn start-server-in-dev-mode
  []
  (start-http-server "8080" "localhost"))
