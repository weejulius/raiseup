(ns main
  (:gen-class)
  (:require [common.component :as component]
            [common.config :as cfg]
            [notes.handler :as notes])
  (:use [ontime.handler]))


(defn -main
  [& args]
  (let [port (nth args 1 "8080")
        host (nth args 0 "localhost")]
    (alter-var-root #'env/system component/init
                    {:id-db-path       (cfg/ret :es :id-db-path)
                     :leveldb-option   (cfg/ret :leveldb-option)
                     :snapshot-db-path (cfg/ret :es :snapshot-db-path)
                     :events-db-path   (cfg/ret :es :events-db-path)
                     :elastic          (cfg/ret :elastic)})
    (alter-var-root #'env/system component/start {
                                                   :port                  port
                                                   :host                  host
                                                   :routes                #'app-routes
                                                   :handlers              [notes/note-event-handlers notes/note-command-handlers]})))

(defn stop
  []
  (alter-var-root #'env/system component/stop {}))
