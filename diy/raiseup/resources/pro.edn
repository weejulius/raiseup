{
  :es {:id-db-path
       "/var/lib/openshift/51c485a94382ecff5b000109/app-root/data/id-db"
      :events-db-path
       "/var/lib/openshift/51c485a94382ecff5b000109/app-root/data/events-db"
      :snapshot-db-path
       "/var/lib/openshift/51c485a94382ecff5b000109/app-root/data/snapshot-db"

      ;;the keys used to persist the id for recovery
      :recoverable-ar-id-key "rvb-ar-id-key"
      :recoverable-event-id-key "rvb-event-id-key"}

 :elastic {:app "raiseup"
           :host "127.6.87.129"
           :port 15555
           :cluster-name "elasticsearch"
           :settings {:index {"number_of_replicas" 1}}
           :mappings
           {"note" {:properties
                    {:ar-id   {:type "long" :store "yes" :index "not_analyzed"}
                     :ar      {:type "string" :store "yes" :index "not_analyzed"}
                     :author  {:type "string" :store "yes" :index "not_analyzed"}
                     :title   {:type "string" :analyzer "standard" :store "yes"}
                     :content {:type "string" :analyzer "standard" :store "yes"}
                     :ctime   {:type "date" :store "yes" :index "not_analyzed"}}}}}
}
