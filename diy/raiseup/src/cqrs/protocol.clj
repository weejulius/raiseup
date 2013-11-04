(ns ^{:doc "the cqrs basic fun"
      :added "1.0"}
  cqrs.protocol)


(defprotocol CommandHandler
  ^{:doc "handle the  command"
    :added "1.0"}
  (handle-command [command]))



(defprotocol Validatable
  "the validation definition"
  (validate [this] "validate the command"))


(defmulti on-event
  "handle the comming event"
  (fn [event] (:event event)))

(defprotocol ReadModel
  "manipulate the read models"
  (load-entry [entry-type entry-id] "return the entry by its type and id")
  (update-entry [entry-type entry-id fn]
    "update then entry with fn to update it")
  (put-entry [entry-type new-entry]
    "update then entry with new entry to update it")
  (remove-entry [entry-type entry-id] "remote the entry")
  (query [entry-type f] "query the entries"))
