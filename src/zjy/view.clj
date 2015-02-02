(ns zjy.view
  (:require [hiccup.core :refer :all]
            [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.form :refer :all]
            [system :as s]
            [common.convert :as convert]
            [common.strs :as strs]
))


(defn layout
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
    (include-css "/css/raiseup.css")
    (include-js "/js/react.js")
    (include-js "/zjy/goog/base.js")
    (include-js "/js/zjy.js")
    [:script {:type "text/javascript"}
     "goog.require(\"zjy.core\");"]]
   [:body
    [:div.container body]
    [:script {:type "text/javascript"}
     "zjy.core.index();"]]))


(defn index
  []
  (layout "index"
          [:span "hello world!!"]
          [:div#role
           [:input#game-server {:type :text :name :game-server :placeholder "输入服务器"}]
           [:input#game-name {:type :text :name :game-name :placeholder "输入角色名字"}]
           [:input#read-role {:type :button :value "读取"}]
           [:div#role-info]
           ]))
