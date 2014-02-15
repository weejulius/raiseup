(ns notes.web.action
  (:require [notes.query :as q]
            [system :as s]
            [common.date :as d])
  )

(defn reg-user
  [name password]
  (let [user (s/fetch (q/->QueryUser :user nil name nil nil))]
    (println "user" user)
    (if-not (empty? user)
      :user-name-is-existing
      (s/send-command :user :create-user
                      {:name     name
                       :password password
                       :ctime    (d/now-as-millis)}))))
