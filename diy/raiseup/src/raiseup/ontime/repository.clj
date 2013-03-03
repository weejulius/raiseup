(ns raiseup.ontime.repository)

(defn record-to-datomic
  "The key between record and datomic is different,take the task for example,
   the key description is mapped to :task/description"
  [record namespace]
  (reduce
   (fn [result key-value]
     (let [key-name (name (first key-value))
           m-value (fnext key-value)
           new-key (keyword (name namespace) key-name)]
       (when-not (= "id" key-name)
         (assoc result new-key m-value))))
   {}
   record))
