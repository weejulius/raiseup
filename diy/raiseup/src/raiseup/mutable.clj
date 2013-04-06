(ns raiseup.mutable)

(def charset "UTF-8")
(def default-leveldb-option {})
(def opened-leveldb (atom {}))
(def eventid-separator ",")
(def eventstore-json-options {})
(def flush-recoverable-id-interval 100000)
