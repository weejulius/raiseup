(ns raiseup.base)

(defn join-str
  ([a-str seperator coll]
      (let [coll-str (map #(str seperator %) coll)]
        (apply str a-str coll-str)))
  ([seperator coll a-str]
     (let [coll-str (map #(str seperator %) coll)]
        (apply str a-str coll-str))))
