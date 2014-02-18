(ns notes.web
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [notes.web.view.index :as v]
            [system :as s]
            [notes.web.action :as action]
            [common.convert :refer [->long ->map ->data ->str]]
            [ring.util.response :refer [redirect redirect-after-post]]
            [common.date :as date]
            [common.validator :as validate]
            [notes.commands :as commands]
            ))


(defroutes notes-routes
           (POST "/commands" []
                 (fn [req]
                   ; (println (type (-> req :params :command)) (-> req :params))
                   (str (s/send-command (read-string (-> req :params :command))))))

           (POST "/" [:as req]
                 (let [result (action/post-note req)]
                   (if (validate/invalid? result)
                     (str result)
                     (redirect-after-post
                       (str "/notes/" result)))))

           (POST "/users" [name password]
                 (let [result (action/reg-user name password)]
                   (if (validate/invalid? result)
                     (str result)
                     (redirect-after-post
                       (str "/notes/" name "/notes")))))


           (POST "/users/login" [name password :as r]
                 (let [result (action/login name password)
                       session   (-> (:session r)
                                     (assoc :identity (keyword name)))]
                   (if (validate/invalid? result)
                     (str result)
                     (do
                       (-> (redirect-after-post
                             (str "/notes/" name "/notes"))
                           (assoc :session session))))))

           (POST "/:ar-id" [ar-id title content]
                 (let [result (s/send-command :note :update-note
                                              {:ar-id   (->long ar-id)
                                               :title   title
                                               :content content
                                               :utime   (date/now-as-millis)})]
                   (redirect-after-post (str "/notes/" ar-id))))

           (GET "/" [page size]
                #_(throw (ex-info "test" {:a 1}))
                (v/index-view {:page (->long page) :size (->long size)}))

           ;;this route must be ahead of /notes/:ar-id
           (GET "/new" []
                (v/new-note-view))

           (GET "/:name/notes" [name]
                (v/user-home-view name))

           (GET "/:ar-id" [ar-id]
                (v/note-view (->long ar-id)))

           (GET "/:ar-id/form" [ar-id :as r]
                (action/note-form-ctrl r))

           (DELETE "/:ar-id" [ar-id]
                   (let [result (s/send-command :note :delete-note
                                                {:ar-id (->long ar-id)})]
                     (redirect (str "/notes")))))
