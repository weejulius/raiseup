(ns raiseup.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [raiseup.ontime.control :refer :all]
            [raiseup.ontime.query :as q]
            [raiseup.reqres :as reqres]
            [common.convert :refer [->long ->map ->str]]
            [common.config :as cfg]
            [clostache.parser :as tpl])
  (:import httl.Engine)
  (:use org.httpkit.server))

(def template-extension-with-dot (str "." (cfg/ret :template-extension)))

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
  "render the template with the params"
  [file-path params]
  (tpl/render-resource
     (str "templates/" file-path template-extension-with-dot)
     params))




(defn handle-asyn-request
  [ring-request]
  (with-channel ring-request channel
    (if (websocket? channel)
      (on-receive
       channel
       (fn [data]
         (let [params (->map data)
               result (handle-request params)]
           (send! channel
                  (->str {:type
                          ;;TODO refactor
                          (clojure.string/replace (name(:type params)) #"-" "_")
                          :data result}))))))))


(defroutes app-routes
  (GET "/todo/slots/new" []
       (render "index"
              (index-view nil)))

  (GET "/todo/slots/edit/:id" [id]
       (render "index"
               (q/find-slot-by-id (->long id))))
  (GET "/ws" []
       handle-asyn-request)

  (route/resources "/")
  (route/not-found "PAGE NOT FOUND"))




