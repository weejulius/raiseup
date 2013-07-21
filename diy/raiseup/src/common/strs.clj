(ns ^{:doc "fn(s) to manipulate string"}
  common.strs)

(defn join-str
  "join a bunch of items with separator
   eg. (join-str ',' [1 3]) => 1,3 "
  ([separator prefix coll]
     (clojure.string/join separator (cons prefix coll))))
