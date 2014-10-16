(defproject raiseup "0.1.0-SNAPSHOT"
  :description "raise up to make to do tool"
  :url "http://red-raiseup.rhcloud.com/"
  :dependencies [[org.clojure/clojure "1.6.0"]

                 ;;utils
                 [cheshire "5.3.1"]
                 [clj-time "0.8.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prismatic/schema "0.3.0"]
                 [com.taoensso/nippy "2.6.3"]
                 [com.taoensso/timbre "3.3.1"]
                 [clj-http "1.0.0"]
                 [hickory "0.5.4"]

                 ;;cqrs
                 [io.vertx/clojure-api "1.0.3"]
                 [com.hazelcast/hazelcast "3.2"]
                 [clojurewerkz/elastisch "2.1.0-beta6"]
                 [org.fusesource.leveldbjni/leveldbjni-all "1.8"]

                 ;;web
                 [http-kit "2.1.19"]
                 [compojure "1.1.9"]
                 [amalloy/ring-gzip-middleware "0.1.3"]
                 [ring/ring-core "1.3.1" :exclusions [org.clojure/tools.reader]]
                 [ring/ring-devel "1.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [hiccup "1.0.5"]
                 [markdown-clj "0.9.47"]
                 [buddy "0.2.0b2"]

                 ;;client
                 [org.clojure/clojurescript "0.0-2371"]
                 [om "0.7.3"]
                 [cljs-ajax "0.3.0"]
                 [prismatic/dommy "0.1.2"]

                 ;;test
                 [criterium "0.4.2" :scope "test"]
                 ]
  :plugins [
            [lein-cljsbuild "1.0.3"]
            [lein-ancient "0.5.5"]]
  :global-vars {*warn-on-reflection* false
                *assert*             false}
  :main main
  :repositories [["sonatype" {:url "https://oss.sonatype.org/content/repositories/snapshots"}]
                 ["ibiblio" {:url "http://mirrors.ibiblio.org/maven2/"}]]

  :profiles
  {:dev {:jvm-opts     []
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
             :compiler     {:preamble     ["reagent/react.js"]
                            :output-dir   "resources/public/out"
                            :output-to    "resources/public/js/client.js"
                            :pretty-print true
                            :optimizations :none
                            :source-map true
                            }}}})
