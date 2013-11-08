(ns ^{:doc "common used functions"}
  common.func)

(defn put-if-absence!
  "load entry from atom data structure, put one if it is not existing,
   the `path` is the path of the nested keys to get the entry"
  [entries path new-entry]
  (swap! entries
         #(update-in % path
                     (fnil identity
                           (if (fn? new-entry)
                             (new-entry)
                             new-entry)))))
