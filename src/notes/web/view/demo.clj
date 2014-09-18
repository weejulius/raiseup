(ns notes.web.view.demo
  (:require [hiccup.page :refer [html5 include-css include-js]]))


(defn- layout
  "the layout of html which can be reused"
  [title & body]
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta
      {:name    "viewport"
       :content "width=device-width, initial-scale=1, maximum-scale=1"
       }]
     [:title title]
     (include-css "/css/demo.css")
;     (include-css "http://fonts.googleapis.com/css?family=Roboto+Slab:400,700,300,100")
     (include-js "/js/react.js")
     (include-js "/out/goog/base.js")
     (include-js "/js/client.js")
     [:script {:type "text/javascript"}
      "goog.require(\"notes.web.client\");"]]
    [:body
     [:div.container body]
     (include-css "/css/hl.css")
     (include-js "/js/highlight.pack.js")
     [:script {:type "text/javascript"}
      "hljs.initHighlightingOnLoad();"]]))


(defn demo-view
  []
  (layout
    "demo"
    [:div.main
     [:div#cmd-box]
     [:div#app]
     [:script {:type "text/javascript"}
      "notes.web.client.demo_ready();"]
     ]))
