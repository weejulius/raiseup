(ns main
  (:use [org.httpkit.server :only [run-server]]
        raiseup.handler)
  (:require [ring.middleware.reload :as reload]
            [compojure.handler :refer [site]]))

(defn -main [& args]
  (let [handler (reload/wrap-reload (site #'app-routes))
        port (Integer/parseInt (nth args 1 "8080"))
        ip (nth args 0 "localhost")]
    (run-server handler {:port port :ip ip})
    (println (str "Server started. listen at " ip ":" port))))
