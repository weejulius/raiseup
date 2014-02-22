(ns notes.web.control
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


(defn- authed?
  [req]
  (not (nil? (-> req :session :identity))))

(defn- current-user
  [req]
  (if-let [user (-> req :session :identity)]
    (name user)))


(defn- current-user?
  [req user-name]
  (= user-name (current-user req)))

(defn- on-authed-with
  ([additional req f]
   (if-not (or (authed? req) additional)
     (throw-notauthorized req)
     (f))))

(defn- on-authed
  [req f]
  (on-authed-with false req f))

(defn- p
  [key req]
  (-> req :params key))

(defn reg-user
  [name password]
  (let [user (s/fetch (q/->QueryUser :user nil name nil nil))]
    (if-not (empty? user)
      (validate/invalid-msg :user-name-is-existing)
      (s/send-command :user :create-user
                      {:name            name
                       :password        password
                       :hashed-password (hash/make-password password)
                       :ctime           (d/now-as-millis)}))))

(defn login
  [name password]
  (let [user (first (s/fetch (q/->QueryUser :user nil name nil nil)))
        user-not-exist? (nil? user)
        valid-password? (and (not user-not-exist?)
                             (not (empty? (:hashed-password user)))
                             (hash/check-password password (:hashed-password user)))]
    (cond
      user-not-exist? (validate/invalid-msg :user-not-found)
      valid-password? (s/send-command :user :login-user
                                      {:ar-id      (:ar-id user)
                                       :login-time (d/now-as-millis)})
      :else (validate/invalid-msg :invalid-password))))

(defn logout
  [req]
  (let [user (s/fetch-first
               (q/->QueryUser :user nil (current-user req) nil nil))]
    (s/send-command :user :logout-user
                    {:ar-id       (:ar-id user)
                     :logout-time (d/now-as-millis)})))


(defn index-ctrl
  [req]
  (let [notes (s/fetch (q/->QueryNote :note nil nil
                                      (->long (p :page req))
                                      (->long (p :size req))))]
    (v/load-index-view (current-user req) notes)))

(defn note-form-ctrl
  [ar-id req]
  (let [new-form? (nil? ar-id)
        note (if-not new-form?
               (s/fetch
                 (q/->QueryNote :note (->long ar-id) nil nil nil)))]
    (on-authed-with
      (not (or new-form?
               (current-user? req (:author note))))
      req
      #(v/load-note-edit-view (current-user req) note))))

(defn user-notes-ctrl
  [name req]
  (on-authed
    req
    #(-> (empty? name)
         (if-not (s/fetch (q/->QueryNote :note nil name nil nil)))
         (v/load-user-notes-view
           (current-user req)
           (current-user? req name)))))

(defn note-ctrl
  [ar-id req]
  (v/load-note-view (current-user req) (->long ar-id)))

(defn post-note
  [req]
  (on-authed
    req
    #(s/send-command :note :create-note
                     {:author  (name (-> req :session :identity))
                      :title   (p :title req)
                      :content (p :content req)
                      :ctime   (d/now-as-millis)})))

(defn put-note
  [req]
  (on-authed
    req
    #(s/send-command :note :update-note
                     {:ar-id   (->long (p :ar-id req))
                      :title   (p :title req)
                      :content (p :content req)
                      :utime   (d/now-as-millis)})))

(defn delete-note
  [req]
  (on-authed
    req
    #(s/send-command :note :delete-note
                     {:ar-id (->long (p :ar-id req))})))