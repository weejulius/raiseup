(ns httpserver
  (:require [common.component :as component]
            [ring.middleware.reload :as reload]
            [ring.middleware.gzip :as gzip]
            [common.core :as common]
            [common.config :as cfg]
            [ring.middleware.pretty-exception :as pretty-exception]
            [compojure.handler :refer [site]]
            [common.logging :as log]
            [ring.util.response :refer [redirect redirect-after-post]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]])
  (:use [org.httpkit.server :only [run-server]]))

(defn unauthorized-handle
  [req meta]
  (if (not (nil? (-> req :session :identity)))
    (redirect "/403")
    (redirect "/401")))


(defrecord HttpKitServer [stop-fn]
  component/Lifecycle
  (init [this options]
    this)
  (start [this options]
    (let [routes-sym (:routes options)
          routes (common/load-sym routes-sym)
          routes (or routes (throw (Exception. (str "fail to load routes for " routes-sym " "  *ns*))))
          handler (or (site routes) (throw (Exception. (str "fail to site routes"))))
          backend (session-backend :unauthorized-handler unauthorized-handle)
          wrapped-handler (-> handler
                              (wrap-authorization backend)
                              (wrap-authentication backend)
                              (wrap-session)
                              gzip/wrap-gzip)
          wrapped-handler (if (cfg/dev-mode?)
                            (-> wrapped-handler
                                reload/wrap-reload
                                pretty-exception/wrap-pretty-exception)
                            wrapped-handler)
          port (:port options)
          ip (:host options)
          stop-fn (run-server wrapped-handler {:port port :ip ip})]
      (assoc this :stop-fn stop-fn)))
  (stop [this options]
    ((:stop-fn this) :timeout 1)
    this)
  (halt [this options]
    this))

