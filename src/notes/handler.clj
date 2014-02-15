(ns notes.handler
  (:require [cqrs.protocol :as p]
            [system :as s]
            [notes.domain :refer :all]
            [common.logging :as log]))

(defn- ar-is-required
  [ar cmd]
  (if (empty? ar)
    (throw (ex-info "ar not found"
                    {:ar      (:ar cmd)
                     :ar-id   (:ar-id cmd)
                     :command cmd}))))

(defn note-command-handlers
  []
  (s/register-command-handler :create-note
                              (fn [ar cmd]
                                (create-note cmd)))

  (s/register-command-handler :update-note
                              (fn [ar cmd]
                                (ar-is-required ar cmd)
                                (update-note ar cmd)))


  (s/register-command-handler :delete-note
                              (fn [ar cmd]
                                (ar-is-required ar cmd)
                                (delete-note ar cmd)))

  (s/register-command-handler :create-user
                              (fn [ar cmd]
                                (create-user ar cmd))))


(defn- update-fn
  [cur-entry event keys]
  (reduce
    (fn [m k]
      (assoc m k (or (k event) (k m))))
    cur-entry
    keys))


(defn note-event-handlers
  []
  (s/register-event-handler :note-updated
                            (fn [event readmodel]
                              (do
                                (p/update-entry
                                  readmodel
                                  (:ar event)
                                  (:ar-id event)
                                  #(update-fn % event [:author :title :content :utime])))))


  (s/register-event-handler :note-deleted
                            (fn [event readmodel]
                              (p/remove-entry
                                readmodel
                                (:ar event)
                                (:ar-id event))))
  (s/register-event-handler :note-created
                            (fn [event readmodel]
                              (p/put-entry readmodel
                                           (select-keys event [:ar :ar-id :author :title :content :ctime]))))

  (s/register-event-handler :user-created
                            (fn [event readmodel]
                              (p/put-entry readmodel
                                           (select-keys event [:ar :ar-id :name :password :ctime])))))
