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
