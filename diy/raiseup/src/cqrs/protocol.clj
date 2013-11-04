(ns ^{:doc "the cqrs protocol"
      :added "1.0"}
  cqrs.protocol)


(defprotocol CommandHandler
  ^{:doc "handle the comming commands from channel"
    :added "1.0"}
  (handle-command [command]))



(defprotocol Validatable
  "the validation definition"
  (validate [this] "validate the command"))


(defmulti on-event
  "handle the comming event"
  (fn [event] (:event event)))

(defprotocol ReadModel
  "manipulate the entries in read model"
  (load-entry [this entry-type entry-id]
    "return the entry by its type and id")
  (update-entry [this entry-type entry-id fn]
    "update then entry with fn to update it")
  (put-entry [this new-entry]
    "update then entry with new entry to update it")
  (remove-entry [this entry-type entry-id] "remote the entry")
  (query [this entry-type f]
    "query the entries, f is used to filter entries"))
