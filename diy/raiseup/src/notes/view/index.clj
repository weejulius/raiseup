(ns notes.view.index
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer  [html5 include-css]]
            [notes.query :refer :all]
            [cqrs.core :as cqrs]))

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
   [:body body]))


(defn mod-notes
  [notes]
  [:div.mod-notes
   [:ul
     (for [note notes]
       [:li
        [:h1 (:title note)]
        [:p (:content note)]])]])

(defn index-view
  []
  (layout "notes" (mod-notes
                            (cqrs/fetch (map->QueryNote {})))))
