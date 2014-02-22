(ns common.component)

(defprotocol Lifecycle
  "manage the lifecycle of component"
  (init [this options] "init the components")
  (start [this options] "start the components or restart the stopped components")
  (stop [this options] "stop the started components")
  (halt [this options] "shutdown the components when the system is to be down"))

