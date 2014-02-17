(ns notes.web.action
  (:require [notes.query :as q]
            [system :as s]
            [common.date :as d]
            [buddy.crypto.signing :as sign]
            [common.validator :as validate]))

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
    (println user)
    (cond
      user-not-exist? (validate/invalid-msg :user-not-found)
      invalid-password? (validate/invalid-msg :invalid-password)
      :else
      (do
        (s/send-command :user :login-user
                        {:ar-id      (:ar-id user)
                         :login-time (d/now-as-millis)})))))