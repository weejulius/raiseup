(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [raiseup.ontime.control :refer :all])
  (:import httl.Engine))

(defn- render
  [file-path params]
  (let [engine (Engine/getEngine)
        template (.getTemplate engine file-path)]
    (.evaluate template params)))

(defroutes app-routes
  (GET "/todo/slots/new" []
       #(render "templates/index.httl" %))
  (POST "/todo/slots" []
        #(render "templates/index.httl" (create-task-slot-action %))))



