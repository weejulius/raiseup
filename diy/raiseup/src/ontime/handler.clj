(ns ontime.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ontime.control :refer :all]
            [ontime.query :as q]
            [notes.commands :refer :all]
            [notes.handler :refer :all]
            [notes.query :refer :all]
            [notes.view.index :as v]
            [cqrs.core :as cqrs]
            [common.reqres :as reqres]
            [common.convert :refer [->long ->map ->str]]
            [common.config :as cfg]
            [ontime.views.common :as vc]
            [ontime.views.index :as vi]
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
       (render "index" (index-view nil)))

  (GET "/todo/slots/edit/:id" [id]
       (render "index"
               (cqrs/fetch (q/map->QuerySlot {:id (->long id)}))))
  (GET "/ws" []
       handle-asyn-request)

  (POST "/notes" [author title content]
        (str (cqrs/send-command
              (->CreateNote :note author title content (java.util.Date.))
             :timeout 2000)))

  (POST "/notes/:ar-id" [ar-id author title content]
       (str (cqrs/send-command
             (->UpdateNote :note (->long ar-id) author title content (java.util.Date.)))))

  (GET "/notes" []
       (v/index-view))

  (GET "/notes/new" []
       (v/new-note-view))

  (route/resources "/")
  (route/not-found "PAGE NOT FOUND"))




