(ns notes.web.action
  (:require [notes.query :as q]
            [system :as s]
            [common.convert :refer [->long]]
            [common.date :as d]
            [notes.web.view.index :as v]
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
                       :password (sign/sign name password)
                       :ctime    (d/now-as-millis)}))))




(defn login
  [name password]
  (let [user (first (s/fetch (q/->QueryUser :user nil name nil nil)))
        user-not-exist? (nil? user)
        invalid-password? (and user (= (:password user) (sign/sign name password)))]
    ;(println user)
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

(defn note-form-ctrl
  [req]
  (let [note (first (s/fetch (q/->QueryNote :note (->long (:ar-id req)) nil nil nil)))]
    (if-not (or (authed? req) (= (:author note) (-> req :session :identity)))
      (throw-notauthorized req)
      (v/note-edit-view note))))


(defn post-note
  [req]
  (if-not (authed? req)
    (do
      (throw-notauthorized req))
    (s/send-command :note :create-note
                    {:author  (-> req :session :identify)
                     :title   (-> req :params :title)
                     :content (-> req :params :content)
                     :ctime   (d/now-as-millis)})))