(ns common.core)

(defn between
  [num low high]
  (and (< num high) (> num low)))


(defn load-sym
  [s]
  (require (symbol (namespace s)))
  (resolve s))

