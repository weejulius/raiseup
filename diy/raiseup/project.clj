(defproject raiseup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.0.0-RC4"]
                 [midje "1.5.0"]
                 [com.datomic/datomic-free "0.8.3848"]
                 [compojure "1.1.5"]
                 [org.clojure/data.json "0.2.1"]
                 [org.fusesource.leveldbjni/leveldbjni-all "1.5"]
                 [com.hazelcast/hazelcast "2.5"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler raiseup.handler/app}
  :profiles  {:dev {:dependencies [[ring-mock "0.1.2"]]
                    :plugins [[lein-midje "3.0.0"]]
                    :repl-options {:port 4001}}})
