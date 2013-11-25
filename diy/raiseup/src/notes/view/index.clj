(ns notes.view.index
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer  [html5 include-css]]
            [hiccup.form :refer :all]
            [notes.query :refer :all]
            [cqrs.core :as cqrs]
            [common.convert :as convert]))

(defn layout
  "the layout of html which can be reused"
  [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta
     {:name "viewport"
      :content "width=device-width, initial-scale=1, maximum-scale=1"
      }]
    [:title title]
    (include-css "/css/raiseup.css")]
   [:body [:div.container body]]))


(defn- mod-nav
  "a nav mod including menus"
  []
  [:div.mod-nav
   [:ul
    [:li "Notes"]
    [:li "Problems"]]])

(defn- mod-notes
  [notes]
  [:div.mod-notes
   [:ul
    (for [note notes]
      [:li
       [:h1 (:ar-id note) "-"  (:title note)] [:span (convert/->str (:ctime note))]
       [:p (:content note)]])]])

(defn index-view
  "the view of index"
  [{:keys [page size]}]
  (let []
    (println "options" page size)
    (layout
     "notes"
     (mod-nav)
     (mod-notes
      (cqrs/fetch (->QueryNote nil nil page size))))))


(defn- mod-new-note
  "form to submit new note"
  [fields]
  (form-to
   [:POST "/notes"]
   [:input {:type "text" :name "title" :value ""}]
   [:input {:type "text" :name "content" :value ""}]
   (submit-button "submit")))

(defn new-note-view
  "the page to new note"
  []
  (layout
   "new note"
   (mod-nav)
   (mod-new-note nil)))
