(ns main
  (:gen-class)
  (:use [org.httpkit.server :only [run-server]]
        raiseup.handler)
  (:require [ring.middleware.reload :as reload]
            [compojure.handler :refer [site]]))

(defn- start-server
  [port-str ip]
  (let [handler (reload/wrap-reload (site #'app-routes))
        port (Integer/parseInt port-str)]
    (run-server handler {:port port :ip ip})
    (println (str "Server started. listen at " ip ":" port))))

(defn -main
  [& args]
  (start-server (nth args 1 "8080") (nth args 0 "localhost")))

(defn start-server-in-dev-mode
  []
  (start-server "8080" "localhost"))
