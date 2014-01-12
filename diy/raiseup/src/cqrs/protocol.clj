(ns ^{:doc "the cqrs protocol"
      :added "1.0"}
  cqrs.protocol)

(defprotocol CommandBus
  ^{:doc "the bus to dispath commands to its channel to handle it"
    :added "1.0"}
  (sends [this command opts] "send command to its channel"))

(defprotocol CommandHandler
  ^{:doc "handle the comming commands from channel"
    :added "1.0"}
  (handle-command [this ar]))

(defprotocol Query
  ^{:doc "queries to talk with the read model"
    :added "1.0"}
  (query [this])
  (find-by-id [this]))


(defprotocol Validatable
  "the validation definition"
  (validate [this] "validate the command"))


(defmulti on-event
  "handle the comming event"
  (fn [event system] (:event event)))

(defprotocol ReadModel
  "manipulate the entries in read model"
  (load-entry [this entry-type entry-id]
    "return the entry by its type and id")
  (update-entry [this entry-type entry-id fn]
    "update then entry with fn to update it")
  (put-entry [this new-entry]
    "update then entry with new entry to update it")
  (remove-entry [this entry-type entry-id] "remote the entry")
  (do-query [this entry-type query]
    "query the entries"))
