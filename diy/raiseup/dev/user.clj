(ns user
  (:require [clojure.tools.namespace.repl :refer
             (refresh refresh-all set-refresh-dirs)]
            [system :as s]
            [common.config :as cfg]
            [common.component :as component])
  (:use  [ontime.handler]))


(defn init []
  (alter-var-root #'s/system component/init
                  {:id-db-path       (cfg/ret :es :id-db-path)
                   :leveldb-option   (cfg/ret :leveldb-option)
                   :snapshot-db-path (cfg/ret :es :snapshot-db-path)
                   :events-db-path   (cfg/ret :es :events-db-path)
                   :elastic          (cfg/ret :elastic)}))

(defn start []
  (alter-var-root #'s/system component/start
                  {:port   "8080"
                   :host   "localhost"
                   :routes #'app-routes}))

(defn stop []
  (alter-var-root #'s/system component/stop {}))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (set-refresh-dirs "src" "resources")
  (refresh :after 'user/go))
