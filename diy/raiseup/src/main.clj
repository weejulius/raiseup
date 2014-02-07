(ns main
  (:gen-class)
  (:require [common.component :as component]))


(defn -main
  [& args]
  (component/go))

(defn stop
  []
  (component/stop-components))
