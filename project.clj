(defproject raiseup "0.1.0-SNAPSHOT"
  :description "raise up to make to do tool"
  :url "http://red-raiseup.rhcloud.com/"
  :dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]

                 ;;utils
                 [cheshire "5.2.0"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [clj-time "0.6.0"]
                 [prismatic/schema "0.2.0"]
                 [com.taoensso/nippy "2.5.2"]
                 [com.taoensso/timbre "3.0.0"]

                 ;;cqrs
                 [io.vertx/clojure-api "1.0.0.Beta2"]
                 [com.hazelcast/hazelcast "3.1.5"]
                 [clojurewerkz/elastisch "1.3.0"]
                 [org.fusesource.leveldbjni/leveldbjni-all "1.7"]

                 ;web
                 [http-kit "2.1.16"]
                 [compojure "1.1.6"]
                 [amalloy/ring-gzip-middleware "0.1.3"]
                 [ring/ring-core "1.2.1" :exclusions [org.clojure/tools.reader]]
                 [ring/ring-devel "1.2.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [hiccup "1.0.5"]
                 [markdown-clj "0.9.41"]
                 [buddy "0.1.0-beta3"]

                 ;;client
                 [org.clojure/clojurescript "0.0-2156"]
                 [reagent "0.3.0"]
                 [om "0.4.2"]
                 [cljs-ajax "0.2.3"]
                 [prismatic/dommy "0.1.2"]

                 ;;test
                 [criterium "0.4.2" :scope "test"]
                 ]
  :plugins [[lein-cljsbuild "1.0.2"] [lein-ancient "0.5.4"]]
  :global-vars {*warn-on-reflection* false
                *assert*             false}
  :main main
  :repositories [["sonatype" {:url "https://oss.sonatype.org/content/repositories/snapshots"}]]

  :profiles
  {:dev {:jvm-opts     ["-Dhttl.reloadable=true"]
         :dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.namespace "0.2.4"]]
         :source-paths ["src" "resources"]
         :plugins      []
         :repl-options {:port 4001}}
   :production
           {:jvm-opts ["-Dproduction=true" "-Dconfig=pro.edn"]}}

  :cljsbuild
  {:builds
    {:client {:source-paths ["src"]
              :compiler
                            {:preamble     ["reagent/react.js"]
                             :output-dir   "resources/public/js"
                             :output-to    "resources/public/js/client.js"
                             :source-map   "resources/public/js/client.js.map"
                             :pretty-print true}}}})
