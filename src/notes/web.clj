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

(defn- on-failed
  [result f]
  (let [invalid? (validate/invalid? result)]
    (if invalid? [false (f result)]
                 [true result])))

(defn- on-success
  [result f]
  (if (first result)
    (f (second result))
    (second result)))

(defroutes notes-routes
           (POST "/commands" []
                 (fn [req]
                   (str (s/send-command (read-string (-> req :params :command))))))

           (POST "/" [:as req]
                 (-> req
                     ctrl/post-note
                     (on-failed (partial str))
                     (on-success #(redirect-after-post
                                   (str "/notes/" %)))))

           (POST "/users" [name password :as r]
                 (-> name
                     (ctrl/reg-user password)
                     (on-failed (partial str))
                     (on-success
                       (fn [result]
                         (-> (redirect-after-post
                               (str "/notes/" name "/notes"))
                             (assoc-in [:session :identity] (keyword name)))))))


           (POST "/users/login" [name password :as r]
                 (-> (ctrl/login name password)
                     (on-failed (partial str))
                     (on-success
                       (fn [result]
                         (-> (redirect-after-post
                               (str "/notes/" name "/notes"))
                             (assoc-in [:session :identity] (keyword name)))))))

           (POST "/:ar-id" [ar-id title content :as req]
                 (-> (ctrl/put-note req)
                     (on-failed (partial str))
                     (on-success
                       #(redirect-after-post (str "/notes/" ar-id)))))

           (DELETE "/:ar-id" [ar-id :as req]
                   (-> (ctrl/delete-note req)
                       (on-failed (partial str))
                       (on-success #(redirect "/notes"))))

           ;;--------------GET ---------------------------------------------


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
                (ctrl/note-form-ctrl ar-id r)))
