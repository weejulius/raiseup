(ns common.date
  "date utils")


(defn ^long now-as-millis
  "the current time as millis"
  []
  (System/currentTimeMillis))
