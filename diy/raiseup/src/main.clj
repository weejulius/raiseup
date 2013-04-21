(ns main
  (:gen-class)
  (:use org.httpkit.server
        raiseup.handler))

(defn -main [& args]
  (run-server (-> app-routes)
              {:port (Integer/parseInt (nth args 1 "8080")) :ip (first args)} )
  (println (str "Server started. listen at " (nth args 1) " "  (first args))))
