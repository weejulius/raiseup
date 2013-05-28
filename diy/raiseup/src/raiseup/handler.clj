(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [raiseup.ontime.control :refer :all]
            [raiseup.ontime.readmodel :as rm]
            [raiseup.reqres :as reqres]
            [raiseup.base :refer [->long ->map ->str]]
            [raiseup.mutable :refer [template-extension]]
            [clostache.parser :as tpl])
  (:import httl.Engine)
  (:use org.httpkit.server))

(def template-extension-with-dot (str "." template-extension))

(defn- render1
  "render the template using the params"
  [file-path params]
  (fn [req]
    (println "render params " params)
   (let [engine (Engine/getEngine)
         template (.getTemplate engine (str file-path template-extension-with-dot))
         template-params (reqres/->template-param params)]
     (.evaluate template template-params))))

(defn- render
  "reader the template using the params"
  [file-path params]
  (tpl/render-resource
     (str "templates/" file-path template-extension-with-dot)
     params))

(defn create-task-slot-req
  [ring-request]
  (with-channel ring-request channel
    (if (websocket? channel)
      (on-receive
       channel
       (fn [data]
         (println "params" (->map data))
         (let [result (create-task-slot-action (:data (->map data)))]
           (println "req result:" result)
           (send! channel (->str {:type :message :data result}))))))))

(defroutes app-routes
  (GET "/todo/slots/new" []
       (render "index" {:unplanned-slots
                          (doall (map
                                  (fn [slot-id] (rm/get-readmodel :task-slot slot-id))
                                  (:none (rm/get-readmodel :user-slot 1))))}))
  (GET "/todo/slots/edit/:id" [id]
       (render "index"
               (rm/get-readmodel :task-slot (->long id))))
  (GET "/todo/slots" [];; [description start-time estimation]
        create-task-slot-req);;(create-task-slot-action description start-time estimation)
  (route/resources "/")
  (route/not-found "PAGE NOT FOUND"))




