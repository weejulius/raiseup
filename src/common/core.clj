(ns common.core)

(defn between
  [num low high]
  (and (< num high) (> num low)))

