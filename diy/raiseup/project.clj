(defproject raiseup "0.1.0-SNAPSHOT"
  :description "raise up to make to do tool"
  :url "http://raiseup-trueyourself.rhcloud.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.13"]
                 [midje "1.6-beta1" :scope "test"]
                 [cheshire "5.2.0"]
                 [com.datomic/datomic-free "0.8.3848"]
                 [compojure "1.1.5"]
                 [ring/ring-devel "1.2.0"]
                 [ring/ring-core "1.2.0"]
                 [clj-time "0.5.1"]
                 [de.ubercode.clostache/clostache "1.3.1"]
                 [httl/httl "1.0.9"]
                 [prismatic/dommy "0.1.1"]
                 [hiccup "1.0.4"]
                 [bouncer "0.2.3-beta1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [com.taoensso/nippy "2.0.0"]
                 [com.taoensso/timbre "2.3.4"]
                 [org.fusesource.leveldbjni/leveldbjni-all "1.7"]
                 [com.hazelcast/hazelcast "2.6"]]
  :plugins [[lein-ring "0.8.5"] [lqein-cljsbuild "0.3.2"]]
  :ring {:handler raiseup.handler/app-routes
         :reload-paths ["src" "resources"]
         :auto-reload? true
         :auto-refresh? false
         :port 8080
         :nrepl {:start? true :port 4001}}
  :main main
  :repositories [["httl" {:url "http://httl.github.io/maven"
                          :checksum :warn}]]
  :profiles  {:dev {:jvm-opts ["-Dhttl.reloadable=true"]
                    :dependencies [[ring-mock "0.1.5"]]
                    :plugins []
                    :repl-options {:port 4001}}
              :production {:jvm-opts ["-Dproduction=true" "-Dconfig=config.pro.clj"]}}
  :cljsbuild {
              :builds [{
                        ; The path to the top-level ClojureScript source directory:
                        :source-paths ["src-cljs"]
                        ; The standard ClojureScript compiler options:
                        ; (See the ClojureScript compiler documentation for details.)
                        :compiler {
                                   :output-to "target/main.js"  ; default: target/cljsbuild-main.js
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
