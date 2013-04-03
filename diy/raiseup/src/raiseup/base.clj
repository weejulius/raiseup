(ns raiseup.base
  (:use [clojure.string :only (join)]))

(defn join-str
  ([separator prefix coll]
     (join separator (cons prefix coll))))
