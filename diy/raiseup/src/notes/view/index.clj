(ns notes.view.index
  (:require [hiccup.core :refer :all]
            [hiccup.element :refer [link-to]]
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
       [:h1
        (link-to (str "/notes/" (:ar-id note)) (:title note))]
       [:span (convert/->str (convert/->date (:ctime note)))]
       [:p (.replace (:content note) "\n" "</br>")]])]])

(defn index-view
  "the view of index"
  [{:keys [page size]}]
  (let []
    (layout
     "notes"
     (mod-nav)
     (mod-notes
      (cqrs/fetch (->QueryNote nil nil page size))))))


(defn- mod-edit-note
  "form to post/put note"
  [note]
  (let [action (str "/notes" (if-not (nil? note) (str "/" (:ar-id note))))]
    [:div.mod-note-form
     (form-to
      [:POST action]
      [:input.title {:type "text" :name "title" :value (get note :title "")}]
      [:textarea.content { :name "content"} (get note :content "")]
      (submit-button "submit"))]))

(defn new-note-view
  "the page to new note"
  []
  (layout
   "new note"
   (mod-nav)
   (mod-edit-note nil)))

(defn note-edit-view
  [ar-id]
  (layout
   "edit note"
   (mod-nav)
   (mod-edit-note
    (cqrs/fetch (->QueryNote ar-id nil nil nil)))))
