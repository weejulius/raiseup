(ns common.component)

(defprotocol Lifecycle
  "manage the lifecycle of component"
  (init [this options])
  (start [this options])
  (stop [this options]))
