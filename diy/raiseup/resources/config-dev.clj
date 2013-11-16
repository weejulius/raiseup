{
 :charset "UTF-8"
 :short-date-format "yyyy-MM-dd"
 :long-date-format "yyyy-MM-dd HH:mm:ss"

 ;; separator for string, like 1,2,3
 :separator ","
 :template-extension "httl"

 ;;option to create leveldb
 :leveldb-option {}

 ;; flush the id to persist how much the recoverable id is increased
 :recoverable-id-flush-interval 100000

 ;;event store configs
 :es {:snapshot-db-path "/tmp/snapshot-db"
      :id-db-path "/tmp/id-db"
      :events-db-path "/tmp/events-db"}}
