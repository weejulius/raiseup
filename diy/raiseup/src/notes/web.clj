(ns notes.web
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [notes.web.view.index :as v]
            [system :as s]
            [common.convert :refer [->long ->map ->str]]
            [ring.util.response :refer [redirect redirect-after-post]]
            [common.date :as date]
            [notes.commands :as commands]))


(defroutes notes-routes
           (POST "/" [author title content]
                 (str (s/send-command :note :create-note
                                      {:author  author
                                       :title   title
                                       :content content
                                       :ctime   (date/now-as-millis)})))

           (POST "/:ar-id" [ar-id title content]
                 (let [result (s/send-command :note :update-note
                                              {:ar-id   (->long ar-id)
                                               :title   title
                                               :content content
                                               :utime   (date/now-as-millis)})]
                   (redirect-after-post (str "/notes/" ar-id))))

           (GET "/" [page size]
                #_(throw (ex-info "test" {:a 1}))
                (v/index-view {:page (or (->long page) 1) :size (or (->long size) 10)}))

           ;;this route must be ahead of /notes/:ar-id
           (GET "/new" []
                (v/new-note-view))

           (GET "/:ar-id" [ar-id]
                (v/note-view (->long ar-id)))

           (GET "/:ar-id/form" [ar-id]
                (v/note-edit-view (->long ar-id)))

           (DELETE "/:ar-id" [ar-id]
                   (let [result (s/send-command :note :delete-note
                                                {:ar-id ar-id})]
                     (redirect (str "/notes")))))
