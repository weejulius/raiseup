(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [raiseup.ontime.control :refer :all]
            [raiseup.ontime.readmodel :as rm]
            [raiseup.reqres :as reqres]
            [raiseup.base :refer [->long]]
            [raiseup.mutable :refer [template-extension]])
  (:import httl.Engine))

(def template-extension-with-dot (str "." template-extension))

(defn- render
  [file-path params]
  (fn [req]
   (let [engine (Engine/getEngine)
         template (.getTemplate engine (str file-path template-extension-with-dot))
         template-params (reqres/->template-param params)]
     (.evaluate template template-params))))

(defroutes app-routes
  (GET "/todo/slots/new" []
       (render "templates/index" {}))
  (GET "/todo/slots/edit/:id" [id]
       (render "templates/index"
              (rm/get-readmodel "task-slot" (->long id))))
  (POST "/todo/slots" [description start-time estimation]
        (create-task-slot-action description start-time estimation)))



