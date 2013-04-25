(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [raiseup.ontime.control :refer :all]
            [raiseup.ontime.views :as view]
            [raiseup.reqres :as reqres]
            [raiseup.base :refer [->long]])
  (:import httl.Engine))

(defn- render
  [file-path params]
  (fn [req]
   (let [engine (Engine/getEngine)
         template (.getTemplate engine file-path)
         template-params (reqres/->template-param params)]
     (.evaluate template template-params))))

(defroutes app-routes
  (GET "/todo/slots/new" []
       (render "templates/index.httl" {}))
  (GET "/todo/slots/edit/:id" [id]
       (render "templates/index.httl"
               (let [task-slot (view/get-view "task-slot"
                                              (->long id))]
                ; (println "geting task slot "  id task-slot)
                 task-slot)))
  (POST "/todo/slots" [description start-time estimation]
        (render "templates/index.httl"
                 (create-task-slot-action description start-time estimation))))



