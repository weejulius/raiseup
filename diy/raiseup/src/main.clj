(ns main
  (:gen-class)
  (:require [system :as s]
            [env :as env])
  (:use  [ontime.handler]))


(defn -main
  [& args]
  (let [port (nth args 1 "8080")
        host (nth args 0 "localhost")]
    (alter-var-root #'s/system env/init {})
    (alter-var-root #'s/system env/start {:port port
                                          :host host
                                          :routes  #'app-routes})))

(defn stop
  []
  (alter-var-root #'s/system env/stop {}))
