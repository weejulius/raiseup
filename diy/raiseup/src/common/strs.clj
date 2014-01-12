(ns ^{:doc "fn(s) to manipulate string"}
  common.strs)

(defn join-str
  "join a bunch of items with separator
   eg. (join-str ',' [1 3]) => 1,3 "
  ([separator prefix coll]
     (clojure.string/join separator (cons prefix coll))))

(defn head
  "get the first n chars from string"
  [^String s length]
  (let [s-length (.length s)
        min-length (min s-length length)]
    (subs s 0 min-length)))
