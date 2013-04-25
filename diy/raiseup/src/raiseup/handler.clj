(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [raiseup.ontime.control :refer :all]
            [raiseup.ontime.views :as view])
  (:import httl.Engine))

(defn- render
  [file-path params]
  (fn [req]
   (let [engine (Engine/getEngine)
         template (.getTemplate engine file-path)
         kw->name (into {} (for [[k v] params]
                             [(clojure.string/replace
                               (name k)
                               #"-"
                               "_") v]))]
     (println "params" kw->name "-" (type kw->name))
     (.evaluate template kw->name))))

(defroutes app-routes
  (GET "/todo/slots/new" []
       (render "templates/index.httl" {}))
  (GET "/todo/slots/edit/:id" [id]
       (render "templates/index.httl"
               (let [task-slot (view/get-view "task-slot"
                                              (Long/parseLong id))]
                 (println "geting task slot "  id task-slot)
                 task-slot)))
  (POST "/todo/slots" [description start-time estimation]
        (render "templates/index.httl"
                 (create-task-slot-action description start-time estimation))))



