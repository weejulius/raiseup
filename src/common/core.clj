(ns common.core)

(defn between
  [num low high]
  (and (< num high) (> num low)))


(defn load-sym
  [s]
 ; (println (str "type of sym" (type s)))
  (require (symbol (namespace s)))
  (resolve s))

