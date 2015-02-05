(defproject raiseup "0.1.0-SNAPSHOT"
  :description "raise up to make to do tool"
  :url "http://red-raiseup.rhcloud.com/"
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [common.web-clj "1.0.0-SNAPSHOT"]
                 [cqrs-clj "1.0.0-SNAPSHOT"]
                 [clj-http "1.0.0"]

                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [hickory "0.5.4"]

                 [hiccup "1.0.5"]
                 [markdown-clj "0.9.47"]

                 ;;client
                 [org.clojure/clojurescript "0.0-2760"]
                 [om "0.7.3"]
                 [cljs-ajax "0.3.0"]
                 [prismatic/dommy "0.1.2"]

                 ;;test
                 [criterium "0.4.2" :scope "test"]

                 ]
  :plugins [[lein-cljsbuild "1.0.4"]
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
         :source-paths ["src"]
         :plugins      []
         :repl-options {:port 4001}}
   :production
   {:jvm-opts ["-Dproduction=true" "-Dconfig=pro.edn"]}

   :uberjar {:resources-paths ["resources"] :omit-source true :aot :all }}

  :clean-targets ^{:protect false} ["resources/public/auto"]

  :resources-path "resources"
  :cljsbuild
  {:builds
   [
    {
     :source-paths ["cljs-src/notes/"]
     :compiler     {
                    :output-to    "resources/public/auto/notes.js"
                    :output-dir   "resources/public/auto/notes"
                    :pretty-print true
                    :optimizations :none
                    :source-map true
                    }},
    {
     :source-paths ["cljs-src/zjy/"]
     :compiler     {:output-to    "resources/public/auto/zjy.js"
                    :output-dir   "resources/public/auto/zjy"
                    :pretty-print true
                    :optimizations :none
                    :source-map true
                    }}
    ]
   })
