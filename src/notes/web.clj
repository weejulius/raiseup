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

           (POST "/users" [name password :as r]
                 (let [result (action/reg-user name password)
                       session (-> (:session r)
                                   (assoc :identity (keyword name)))]
                   (if (validate/invalid? result)
                     (str result)
                     (-> (redirect-after-post
                           (str "/notes/" name "/notes"))
                         (assoc :session session)))))


           (POST "/users/login" [name password :as r]
                 (let [result (action/login name password)
                       session (-> (:session r)
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

           (GET "/" [:as req]
                #_(throw (ex-info "test" {:a 1}))
                (action/index-ctrl req))

           (GET "/users/logout" [:as req]
                (action/logout req)
                (-> (redirect "/notes")
                    (assoc :session {})))

           ;;this route must be ahead of /notes/:ar-id
           (GET "/new" [:as r]
                (action/note-form-ctrl nil r))

           (GET "/:name/notes" [name :as r]
                (action/user-notes-ctrl name r))

           (GET "/:ar-id" [ar-id :as req]
                (action/note-ctrl ar-id req))

           (GET "/:ar-id/form" [ar-id :as r]
                (action/note-form-ctrl ar-id r))

           (DELETE "/:ar-id" [ar-id]
                   (let [result (s/send-command :note :delete-note
                                                {:ar-id (->long ar-id)})]
                     (redirect (str "/notes")))))
