(ns main
  (:gen-class)
  (:require [common.components :as component]
            [common.core :as common]))


(defn -main
  [& args]
  (resolve (symbol "web" "app-routes"))
  (component/go- :http-server {:host (nth args 0 "localhost")
                               :port (Integer/parseInt (nth args 1 "8080"))}))

(defn stop
  []
  (component/stop-))

(defn refresh
  []
  (component/refresh-))

(defn shutdown
  []
  (component/shutdown-))

