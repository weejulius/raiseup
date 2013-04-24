(defproject raiseup "0.1.0-SNAPSHOT"
  :description "raise up to make to do tool"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.0.1"]
                 [midje "1.5.0"]
                 [com.datomic/datomic-free "0.8.3848"]
                 [compojure "1.1.5"]
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [httl/httl "1.0.9"]
                 [com.taoensso/nippy "1.1.0"]
                 [org.fusesource.leveldbjni/leveldbjni-all "1.6.1"]
                 [com.hazelcast/hazelcast "2.5"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler raiseup.handler/app-routes}
  :main main
  :profiles  {:dev {:dependencies [[ring-mock "0.1.2"]]
                    :plugins []
                    :repl-options {:port 4001}}})
