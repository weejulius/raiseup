(ns raiseup.eventbus)


(defn ->sent
  ^{:doc "send events to their listeners"}
  [event event-router]
  (doseq [queued-listeners ((:event event) event-router)]
    (dorun (pmap #(% event) queued-listeners))))

(defn ->sub
  ^{:doc "subscribe event to listeners"}
  [event-name listener])
