{
  :http-server       {:component httpserver/->HttpKitServer
                      :host      "localhost"
                      :port      8080
                      :routes    #'web/app-notes
                      }
  :snapshot-db       {
                       :component      cqrs.storage/->LeveldbStore
                       :path           "/tmp/snapshot-db"
                       :leveldb-option {}
                       }
  :recoverable-id-db {
                       :component      cqrs.storage/->LeveldbStore
                       :path           "/tmp/ids-db"
                       :leveldb-option {}
                       }

  :events-db         {
                       :component      cqrs.storage/->LeveldbStore
                       :path           "/tmp/events-db"
                       :leveldb-option {}
                       }
  :recoverable-ids   {
                       :component                     cqrs.storage/->RecoverableLongId
                       :storage                       :recoverable-id-db
                       :recoverable-id-flush-interval 100000
                       }

  :bus               {
                       :component cqrs.vertx/->VertxBus
                       :handlers  [notes.handler/note-event-handlers notes.handler/note-command-handlers]
                       }
  :readmodel         {
                       :component    cqrs.elastic-rm/->ElasticReadModel
                       :app          "raiseup"
                       :host         "127.0.0.1"
                       :port         9300
                       :cluster-name "elasticsearch"
                       ;  :settings {:index {"number_of_replicas" 1}}
                       :mappings
                                     {"note" {:properties
                                               {:ar-id   {:type "long" :store "yes" :index "not_analyzed"}
                                                :ar      {:type "string" :store "yes" :index "not_analyzed"}
                                                :author  {:type "string" :store "yes" :index "not_analyzed"}
                                                :title   {:type "string" :analyzer "standard" :store "yes"}
                                                :content {:type "string" :analyzer "standard" :store "yes"}
                                                :ctime   {:type "date" :store "yes" :index "not_analyzed"}}}}}
  }

