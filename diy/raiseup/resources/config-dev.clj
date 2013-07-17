{
 :charset "UTF-8"
 :short-data-format "yyyy-MM-dd"
 :long-date-format "yyyy-MM-dd HH:mm:ss"
 ;; separator for string, like 1,2,3
 :separator ","
 :template-extension "httl"

 ;;option to create leveldb
 :leveldb-option {}

 ;; flush the id to persist how much the recoverable id is increased
 :recoverable-id-flush-interval 100000

 ;;event store configs
 :es {:event-id-db-path "/tmp/event-ids-real"
      :events-db-path "/tmp/events-real"
      ;;the key used to persist the id for recovery
      :recoverable-ar-id-key "rvb-ar-id-key"
      :recoverable-event-id-key "rvb-event-id-key"}}
