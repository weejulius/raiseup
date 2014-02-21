(ns notes.web
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [notes.web.view.index :as v]
            [system :as s]
            [notes.web.control :as ctrl]
            [common.convert :refer [->long ->map ->data ->str]]
            [ring.util.response :refer [redirect redirect-after-post]]
            [common.date :as date]
            [common.validator :as validate]
            [notes.commands :as commands]
            ))


(defn- on-command-result [result on-invalid on-valid]
  (if (validate/invalid? result)
    (on-invalid result)
    (on-valid result)))

(defroutes notes-routes
           (POST "/commands" []
                 (fn [req]
                   (str (s/send-command (read-string (-> req :params :command))))))

           (POST "/" [:as req]
                 (-> req
                     ctrl/post-note
                     (on-command-result
                       #(str %)
                       #(redirect-after-post
                         (str "/notes/" %)))))

           (POST "/users" [name password :as r]
                 (-> name
                     (ctrl/reg-user password)
                     (on-command-result
                       #(str %)
                       #(-> (redirect-after-post
                              (str "/notes/" name "/notes"))
                            assoc :session (assoc (:session r) :identity (keyword name))))))


           (POST "/users/login" [name password :as r]
                 (let [result (ctrl/login name password)
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
                (ctrl/index-ctrl req))

           (GET "/users/logout" [:as req]
                (ctrl/logout req)
                (-> (redirect "/notes")
                    (assoc :session {})))

           ;;this route must be ahead of /notes/:ar-id
           (GET "/new" [:as r]
                (ctrl/note-form-ctrl nil r))

           (GET "/:name/notes" [name :as r]
                (ctrl/user-notes-ctrl name r))

           (GET "/:ar-id" [ar-id :as req]
                (ctrl/note-ctrl ar-id req))

           (GET "/:ar-id/form" [ar-id :as r]
                (ctrl/note-form-ctrl ar-id r))

           (DELETE "/:ar-id" [ar-id]
                   (let [result (s/send-command :note :delete-note
                                                {:ar-id (->long ar-id)})]
                     (redirect (str "/notes")))))
