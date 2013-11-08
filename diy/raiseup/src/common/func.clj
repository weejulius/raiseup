(ns ^{:doc "common used functions"}
  common.func)

(defn cond-update
  "update entry with new if pre? is satisfied"
  [new-entry pre?]
  (fn [entry]
    (if (pre? entry)
      (if (fn? new-entry)
        (new-entry)
        new-entry)
      entry)))

(defn put-if-absence
  [m path new-entry]
  (update-in m path
             #((cond-update new-entry nil?) %)))

(defn put-if-absence!
  "load entry from atom data structure, put one if it is not existing,
   the `path` is the path of the nested keys to get the entry"
  [entries path new-entry]
  (swap! entries
         (fn [m] (put-if-absence m path new-entry))))
