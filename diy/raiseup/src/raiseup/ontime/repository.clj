(ns raiseup.ontime.repository)

(defn record-to-datomic
  "The key of properties between record and datomic is different,take the task for example,
   the key of property description is mapped to :task/description"
  [record namespace]
  (reduce
   (fn [result property]
     (let [pro-name (name (first property))
           pro-value (fnext property)
           pro-name-for-datomic (keyword (name namespace) pro-name)]
       (when-not (= "id" pro-name)
         (assoc result pro-name-for-datomic pro-value))))
   {}
   record))
