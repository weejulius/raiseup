(ns ^{:doc "fn(s) to manipulate string"}
  common.strs)

(defn join-str
  "join a bunch of items with separator
   eg. (join-str ',' [1 3]) => 1,3 "
  ([separator prefix coll]
   (clojure.string/join separator (cons prefix coll))))

(defn head
  "get the first n chars from string"
  ([^String s length append]
   (let [s-length (.length s)]
     (if (> s-length length)
       (str (subs s 0 length) append)
       s)))
  ([s length]
   (head s length nil)))
