(ns ^{:doc   "the cqrs protocol"
      :added "1.0"}
  cqrs.protocol
  (:refer-clojure :exclude [send]))

(defprotocol Bus
  ^{:doc   "the bus to dispath messages (like commands,events) to their handler"
    :added "1.0"}
  (sends [this command options] "send command to its handler")
  (publish [this event] "publish event to this handlers")
  (reg [this name handle] "register handler for the event/command type of ar"))


(defprotocol Query
  ^{:doc   "queries to talk with the read model"
    :added "1.0"}
  (query [this] "query the entries by options"))


(defprotocol ReadModel
  "manipulate the entries in read model"
  (load-entry [this entry-type entry-id]
              "return the entry by its type and id")
  (update-entry [this entry-type entry-id fn]
                "update then entry with fn to update it")
  (put-entry [this new-entry]
             "update then entry with new entry to update it")
  (remove-entry [this entry-type entry-id] "remote the entry")
  (do-query [this entry-type query] "query the entries"))

