(ns main
  (:gen-class)
  (:require [common.component :as component]))


(defn -main
  [& args]
  (component/go- {:http-server {:host (nth args 0 "localhost")
                                :port (Integer/parseInt (nth args 1 "8080"))}}))

(defn stop
  []
  (component/stop- nil))

(defn refresh
  []
  (component/refresh- nil))

(defn shutdown
  []
  (component/shutdown- nil))