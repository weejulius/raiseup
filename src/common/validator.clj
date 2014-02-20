(ns common.validator)


(defn invalid-msg
  [key & {:as opts}]
  {:key  key
   :opts opts
   :type :invalid-message})

(defn invalid?
  [msg]
  (= :invalid-message (:type msg)))


