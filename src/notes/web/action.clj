(ns notes.web.action
  (:require [notes.query :as q]
            [system :as s]
            [common.convert :refer [->long]]
            [common.date :as d]
            [notes.web.view.index :as v]
            [clojure.pprint :as pr]
            [buddy.crypto.hashers.bcrypt :as hash]
            [buddy.crypto.signing :as sign]
            [common.validator :as validate]
            [buddy.auth :refer [authenticated? throw-notauthorized]]))

(defn reg-user
  [name password]
  (let [user (s/fetch (q/->QueryUser :user nil name nil nil))]
    (if-not (empty? user)
      (validate/invalid-msg :user-name-is-existing)
      (s/send-command :user :create-user
                      {:name     name
                       :password (hash/make-password password)
                       :ctime    (d/now-as-millis)}))))

(defn login
  [name password]
  (let [user (first (s/fetch (q/->QueryUser :user nil name nil nil)))
        user-not-exist? (nil? user)
        invalid-password? (and (not user-not-exist?)
                               (not (hash/check-password password (:password user))))]
    ; (println user)
    (cond
      user-not-exist? (validate/invalid-msg :user-not-found)
      invalid-password? (validate/invalid-msg :invalid-password)
      :else
      (s/send-command :user :login-user
                      {:ar-id      (:ar-id user)
                       :login-time (d/now-as-millis)}))))

(defn- authed?
  [req]
  (not (nil? (-> req :session :identity))))

(defn- current-user
  [req]
  (if-let [user (-> req :session :identity)]
    (name user)))


(defn- current-user?
  [req user-name]
  (println user-name (current-user req))
  (= user-name (current-user req)))

(defn index-ctrl
  [req]
  (let [notes (s/fetch (q/->QueryNote :note nil nil
                                      (->long (-> req :params :page))
                                      (->long (-> req :params :size))))]
    (v/index-view (current-user req) notes)))

(defn note-form-ctrl
  [ar-id req]
  (let [new-form? (nil? ar-id)
        note (if-not new-form?
               (s/fetch
                 (q/->QueryNote :note (->long ar-id) nil nil nil)))]
    (pr/pprint req)
    (if-not (and
              (authed? req)
              (or new-form?
                  (current-user? req (:author note))))
      (throw-notauthorized req)
      (v/note-edit-view (current-user req) note))))

(defn user-notes-ctrl
  [name req]
  (let [notes (if-not (empty? name)
                (s/fetch (q/->QueryNote :note nil name nil nil)))]
    (if-not (authed? req)
      (throw-notauthorized req)
      (v/user-notes-view notes name (current-user? req name)))))

(defn note-ctrl
  [ar-id req]
  (v/note-view (current-user req) (->long ar-id)))

(defn post-note
  [req]
  (if-not (authed? req)
    (do
      (throw-notauthorized req))
    (s/send-command :note :create-note
                    {:author  (name (-> req :session :identity))
                     :title   (-> req :params :title)
                     :content (-> req :params :content)
                     :ctime   (d/now-as-millis)})))