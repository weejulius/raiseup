(ns notes.handler
  (:require [cqrs.protocol :as p]
            [system :as s]
            [notes.domain :refer :all]
            [common.logging :as log]))



(defn note-command-handlers
  []
  (s/register-command-handler :create-note
                              (fn [ar cmd]
                                (create-note cmd)))

  (s/register-command-handler :update-note
                              (fn [ar cmd]
                                (update-note ar cmd)))


  (s/register-command-handler :delete-note
                              (fn [ar cmd]
                                (delete-note ar cmd)))

  (s/register-command-handler :create-user
                              (fn [ar cmd]
                                (create-user ar cmd)))

  (s/register-command-handler :login-user
                              (fn [ar cmd]
                                (login-user ar cmd)))

  (s/register-command-handler :logout-user
                              (fn [ar cmd]
                                (logout-user ar cmd))))


(defn- update-fn
  [cur-entry event keys]
  (reduce
    (fn [m k]
      (assoc m k (or (k event) (k m))))
    cur-entry
    keys))


(defn- update-fields
  [fields]
  (fn [event readmodel]
    (do
      (p/update-entry
        readmodel
        (:ar event)
        (:ar-id event)
        #(update-fn % event fields)))))

(defn- put-fields
  [fields]
  (fn [event readmodel]
    (p/put-entry readmodel
                 (select-keys event fields))))

(defn note-event-handlers
  []
  (s/register-event-handler :note-updated
                            (update-fields [:author :title :content :utime]))


  (s/register-event-handler :note-deleted
                            (fn [event readmodel]
                              (p/remove-entry
                                readmodel
                                (:ar event)
                                (:ar-id event))))
  (s/register-event-handler :note-created
                            (put-fields [:ar :ar-id :author :title :content :ctime]))

  (s/register-event-handler :user-created
                            (put-fields [:ar :ar-id :name :hashed-password :ctime]))

  (s/register-event-handler :user-logined
                            (update-fields [:login-time]))

  (s/register-event-handler :user-logouted
                            (update-fields [:logout-time])))
