(ns ontime.handler
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [ring.util.response :refer [redirect redirect-after-post]]
            [compojure.route :as route]
            [ontime.control :refer :all]
            [ontime.query :as q]
            [notes.commands :refer :all]
            [notes.query :refer :all]
            [notes.view.index :as v]
            [cqrs.core :as cqrs]
            [system :as s]
            [common.reqres :as reqres]
            [common.convert :refer [->long ->map ->str]]
            [common.config :as cfg]
            [common.logging :as log]
            [ontime.views.common :as vc]
            [ontime.views.index :as vi]
            [clostache.parser :as tpl])
  (:import httl.Engine)
  (:use org.httpkit.server
        notes.handler))

(def template-extension-with-dot (str "." (cfg/ret :template-extension)))

;(defn- render1
;  "render the template using the params"
;  [file-path params]
;  (fn [req]
;    (log/debug "render params " params)
;   (let [engine (Engine/getEngine)
;         template (.getTemplate engine (str file-path template-extension-with-dot))
;         template-params (reqres/->template-param params)]
;     (.evaluate template template-params))))

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
                                             (clojure.string/replace (name (:type params)) #"-" "_")
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
                 (str (s/send-command
                        (->CreateNote :note author title content (java.util.Date.))
                        :timeout 2000)))

           (POST "/notes/:ar-id" [ar-id author title content]
                 (let [result (s/send-command
                                (->UpdateNote :note
                                              (->long ar-id)
                                              author
                                              title
                                              content
                                              (java.util.Date.)))]
                   (if-not (nil? (:ok? result)) (str result)
                                                (redirect-after-post (str "/notes/" ar-id)))))

           (GET "/notes" [page size]
                (v/index-view {:page (or (->long page) 1) :size (or (->long size) 10)}))

           ;;this route must be ahead of /notes/:ar-id
           (GET "/notes/new" []
                (v/new-note-view))

           (GET "/notes/:ar-id" [ar-id]
                (v/note-view (->long ar-id)))

           (GET "/notes/:ar-id/form" [ar-id]
                (v/note-edit-view (->long ar-id)))

           (DELETE "/notes/:ar-id" [ar-id]
                   (let [result (s/send-command
                                  (->DeleteNote :note
                                                (->long ar-id)))]
                     (if-not (nil? (:ok? result)) (str result)
                                                  (redirect (str "/notes")))))

           (route/resources "/")
           (route/not-found "PAGE NOT FOUND"))




