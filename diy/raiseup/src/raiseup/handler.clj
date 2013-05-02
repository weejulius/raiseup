(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [raiseup.ontime.control :refer :all]
            [raiseup.ontime.readmodel :as rm]
            [raiseup.reqres :as reqres]
            [raiseup.base :refer [->long ->map ->str]]
            [raiseup.mutable :refer [template-extension]])
  (:import httl.Engine)
  (:use org.httpkit.server))

(def template-extension-with-dot (str "." template-extension))

(defn- render
  [file-path params]
  (fn [req]
   (let [engine (Engine/getEngine)
         template (.getTemplate engine (str file-path template-extension-with-dot))
         template-params (reqres/->template-param params)]
     (.evaluate template template-params))))

(defn create-task-slot-req
  [ring-request]
  (with-channel ring-request channel
    (if (websocket? channel)
      (on-receive channel
                  (fn [data]
                    (println data)
                    (println "param:" (->map data))
                    (let [{:keys [description start-time estimation]} (->map data)
                          result (create-task-slot-action
                                  description
                                  start-time
                                  estimation)]
                      (send! channel (->str {:type :message :data result})))))
      (send! channel {:status 200
                      :headers {"Content-Type" "text/plain"}
                      :body    "Long polling?"}))))

(defroutes app-routes
  (GET "/todo/slots/new" []
       (render "templates/index" {}))
  (GET "/todo/slots/edit/:id" [id]
       (render "templates/index"
               (rm/get-readmodel "task-slot" (->long id))))
  (GET "/todo/slots" [];; [description start-time estimation]
        create-task-slot-req);;(create-task-slot-action description start-time estimation)
  (route/resources "/")
  (route/not-found "PAGE NOT FOUND"))




