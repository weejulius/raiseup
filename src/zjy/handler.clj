(ns zjy.handler
  (:require [cqrs.protocol :as p]
            [system :as s]
            [zjy.domain :refer :all]
            [zjy.lol :as lol]
            [common.logging :as log]))


(defn zjy-command-handlers []
  (s/register-command-handler
   :active-account
   (fn [ar cmd]
     (active-account ar cmd))))


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


(defn handle-account-activated
  [event readmodel]
  (log/debug "account activated" event)
  (let [game-info (lol/fetch-summor-info (:game-server event) (:game-name event))
      ;  game-info (assoc game-info :ranked (str (:ranked game-info)))
      ;  game-info (assoc game-info :match (str (:match game-info)))
        person-info  (select-keys event
                                  [:ar :ar-id :game-name :game-server :dirty-words :age :ctime])
        account (merge game-info person-info)]
    (log/debug "info" account)
    (p/put-entry readmodel account) ))

(defn zjy-event-handlers []
  (s/register-event-handler
   :account-activated
   handle-account-activated))
