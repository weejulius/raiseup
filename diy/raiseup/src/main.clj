(ns main
  (:gen-class)
  (:require [common.component :as component]))


(defn -main
  [& args]
  (component/go- nil nil))

(defn stop
  []
  (component/stop- nil nil))

(defn refresh
  []
  (component/refresh- nil nil))

(defn shutdown
  []
  (component/shutdown- nil nil))