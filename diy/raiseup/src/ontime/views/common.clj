(ns ontime.views.common
  (:require [hiccup.page :refer [html5 include-css]]))


(defn layout
  "the layout of html which can be reused"
  [title body]
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
     [:title title]
     (include-css "/css/raiseup.css")]
    (body)))
