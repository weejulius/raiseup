(ns notes.web.control
  (:require [notes.query :as q]
            [system :as s]
            [common.convert :refer [->long]]
            [common.date :as d]
            [notes.web.view.index :as v]
            [clojure.pprint :as pr]
            [buddy.hashers.bcrypt :as hash]
            [common.validator :as validate]
            [buddy.auth :refer [authenticated? throw-unauthorized]]))


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

(defn- authorize-to-change-resource
  ([has-resource? resource-owner req]
   (if-not (or (authed? req) (and has-resource?
                                  (= (current-user req) (resource-owner))))
     (throw-unauthorized req))))

(defn- authorize-resource
  [req]
  (authorize-to-change-resource false nil req))

(defn- p
  [key req]
  (-> req :params key))

(defn reg-user
  [name password]
  (let [user (s/fetch :user :name name)]
    (if-not (empty? user)
      (validate/invalid-msg :user-name-is-existing)
      (s/send-command :user :create-user
                      {:name            name
                       :password        password
                       :hashed-password (hash/make-password password)
                       :ctime           (d/now-as-millis)}))))

(defn login
  [name password]
  (let [user (s/fetch-first :user :name name)
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
  (let [user (s/fetch-first :user :name (current-user req))]
    (s/send-command :user :logout-user
                    {:ar-id       (:ar-id user)
                     :logout-time (d/now-as-millis)})))


(defn index-ctrl
  [req]
  (let [notes (s/fetch :note :page (->long (p :page req)) :size (->long (p :size req)))]
    (v/load-index-view (current-user req) notes)))

(defn note-form-ctrl
  [ar-id req]
  (let [new-form? (nil? ar-id)
        note (if-not new-form? (s/fetch :note :ar-id (->long ar-id)))]
    (authorize-to-change-resource new-form? #(:author note) req)
    (v/load-note-edit-view (current-user req) note)))

(defn user-notes-ctrl
  [name req]
  (-> (if-not (empty? name)
        (s/fetch :note :author name))
      (v/load-user-notes-view
        (current-user req)
        (current-user? req name))))

(defn note-ctrl
  [ar-id req]
  (v/load-note-view (current-user req) (->long ar-id)))

(defn post-note
  [req]
  (authorize-resource req)
  (s/send-command :note :create-note
                  {:author  (name (-> req :session :identity))
                   :title   (p :title req)
                   :content (p :content req)
                   :ctime   (d/now-as-millis)}
                  :timeout 3000))

(defn put-note
  [req]
  (let [ar-id (->long (p :ar-id req))
        get-note #(s/fetch :note :ar-id ar-id)]
    (authorize-to-change-resource (nil? ar-id) get-note req)
    (s/send-command :note :update-note
                    {:ar-id   ar-id
                     :title   (p :title req)
                     :content (p :content req)
                     :utime   (d/now-as-millis)}
                    :timeout 3000)))

(defn delete-note
  [req]
  (let [ar-id (->long (p :ar-id req))
        get-note #(s/fetch :note :ar-id ar-id)]
    (authorize-to-change-resource (nil? ar-id) get-note req)
    (s/send-command :note :delete-note
                    {:ar-id (->long (p :ar-id req))})))

