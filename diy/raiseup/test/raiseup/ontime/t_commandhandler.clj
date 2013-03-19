(ns raiseup.ontime.t-commandhandler
  (:use midje.sweet
        raiseup.ontime.commandhandler))

(def leveldb (open-leveldb "/tmp/leveldb-test" {}))

(fact (write-to-leveldb leveldb "hello" "word") => nil)

(fact "open level db"
      (open-leveldb "/tmp/leveldb" {}) => (complement nil?))

(fact "pad 0 to number"
      (pad-number 1 2 0) => "01")

(fact "the store key is consist of length of ar name,ar name and the ar id"
      (construct-store-key "task" 123455) => "04task123455")





;; (fact "write data to leveldb"
  ;;    (write-to-leveldb leveldb "jyu" "hello word") => (complement nil?))
