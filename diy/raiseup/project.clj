(defproject raiseup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.0-RC16"]
                 [http-kit "2.0.0-RC1"]
                 [midje "1.5-beta2"]
                 [compojure "1.1.5"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler raiseup.handler/app}
  :profiles  {:dev {:dependencies [[ring-mock "0.1.2"]]
                    :plugins [[lein-midje "3.0-beta1"]]}})
