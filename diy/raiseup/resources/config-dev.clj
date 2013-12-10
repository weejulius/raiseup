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
      :events-db-path "/tmp/events-db"}

 :elastic {:app "raiseup"
           :url "http://127.0.0.1:9200"
           :settings {:index {:number_of_replicas 1}}
           :mappings
           {"note" {:properties
                    {:ar-id   {:type "long" :store "yes" :index "not_analyzed"}
                     :ar      {:type "string" :store "yes" :index "not_analyzed"}
                     :author  {:type "string" :store "yes" :index "not_analyzed"}
                     :title   {:type "string" :analyzer "standard" :store "yes"}
                     :content {:type "string" :analyzer "standard" :store "yes"}
                     :ctime   {:type "date" :store "yes" :index "not_analyzed"}}}}}
 }
