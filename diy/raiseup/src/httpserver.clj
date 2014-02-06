(ns httpserver
  (:require [common.component :as component]
            [ring.middleware.reload :as reload]
            [ring.middleware.gzip :as gzip]
            [compojure.handler :refer [site]]
            [common.logging :as log])
  (:use [org.httpkit.server :only [run-server]]))

(defrecord HttpKitServer [stop-fn]
  component/Lifecycle
  (init [this options]
    this)
  (start [this options]
    (let [handler (site (:routes options))
          wrapped-handler (gzip/wrap-gzip
                            (reload/wrap-reload handler))
          port (:port options)
          ip (:host options)
          stop-fn (run-server wrapped-handler {:port port :ip ip})]
      (log/info (str "Server started at " ip ":" port))
      (assoc this :stop-fn stop-fn)))
  (stop [this options]
    ((:stop-fn this) :timeout 1)))