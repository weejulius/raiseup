(ns raiseup.ontime.t-commandhandler
  (:use midje.sweet
        raiseup.ontime.commandhandler))

(def leveldb (open-leveldb "/tmp/leveldb-test11" {}))

(fact (write-to-leveldb leveldb "hello" "word") => nil)

(fact "open level db"
      (open-leveldb "/tmp/leveldb" {}) => (complement nil?))

(fact "pad aggregate root name to fixed length of string"
      (pad-aggregate-root-name (name :task) 20 \space)
      => "task                "
      (pad-aggregate-root-name "task12345678910123444" 20 \space)
      => "task1234567891012344")

(fact "store an event"
      (store-aggregate-root
       {:aggregate-root :task
        :id 100
        :events [{:event :task-created
                  :name "jyu"}]}))



;; (fact "write data to leveldb"
  ;;    (write-to-leveldb leveldb "jyu" "hello word") => (complement nil?))
