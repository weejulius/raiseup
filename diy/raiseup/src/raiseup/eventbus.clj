(ns raiseup.eventbus)


(defn ->sent
  ^{:doc "send events to their listeners, if the execution
          of event does not take much time, do not use pararellasm"}
  [event event-router]
  (doseq [queued-listeners ((:event event) event-router)]
    (if-let [pros (last queued-listeners)]
      (condp = pros))))

(defn ->sub
  ^{:doc "subscribe event to listeners"}
  [event-name listener])
