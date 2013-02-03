(defproject raiseup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [http-kit "2.0.0-RC1"]
                 [speclj "2.5.0"]
                 [compojure "1.1.1"]]
  :plugins [[lein-ring "0.7.1"]
            [speclj "2.5.0"]]
  :test-paths ["spec/"]
  :ring {:handler raiseup.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.2"]]}})
