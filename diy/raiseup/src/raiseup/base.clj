(ns raiseup.base)

(defn join-str
  ([coll separator]
      (let [first (first coll)
            rest (next coll)]
        (apply str first (reduce #(conj %1 "," %2) [] rest)))))
