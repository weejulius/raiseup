(ns raiseup.ontime.t-commandhandler
  (:use midje.sweet
        raiseup.ontime.commandhandler))

(def leveldb (open-leveldb "/tmp/leveldb-test10" {}))

                                        ;(fact (write-to-leveldb leveldb "hello" "word") => nil)
(fact "open level db"
      (open-leveldb "/tmp/leveldb" {}) => (complement nil?))

(fact "open level db for aggregate root"
      ((open-leveldb-for-ar level-db-root-dir {}) "hello") => (complement nil?))

(fact "store aggregate root"
      (store-aggregate-root
       {:ar-name :task3
        :ar-id 10001
        :events [{:event :task-created
                  :name "test"}]} (open-leveldb-for-ar level-db-root-dir {})) =>
                  nil?)

(fact "performance"
      (println
       (time
        (dotimes [n 1000000]
          (.put leveldb (to-bytes (str n) "UTF-8") (to-bytes "word" "UTF-8"))
          )))
      => nil?)
