(ns notes.view.index
  (:require [hiccup.core :refer :all]
            [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5 include-css]]
            [hiccup.form :refer :all]
            [notes.query :refer :all]
            [system :as s]
            [markdown.core :as markdown]
            [common.convert :as convert]
            [common.strs :as strs]))

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
     (include-css "http://fonts.googleapis.com/css?family=Fjalla+One")]
    [:body [:div.container body]]))


(defn- mod-nav
  "a nav mod including menus"
  []
  [:div.mod-nav
   [:ul
    [:li (link-to "/notes" "Notes")]
    [:li "Problems"]]])

(defn- mod-notes
  [notes]
  [:div.mod-notes
   [:ul
    (for [note notes]
      [:li
       [:h1
        (link-to (str "/notes/" (:ar-id note) "/form") (:title note))]
       [:span (convert/->str (convert/->date (:ctime note)))]
       [:div.markdown
        (markdown/md-to-html-string
          (strs/head (:content note) 1000))]])]])

(defn index-view
  "the view of index"
  [{:keys [page size]}]
  (let []
    (layout
      "notes"
      (mod-nav)
      (mod-notes
        (s/fetch (->QueryNote nil nil page size))))))


(defn- mod-form-note
  "form to post/put note"
  [note]
  (let [action (str "/notes" (if-not (nil? note) (str "/" (:ar-id note))))]
    [:div.mod-note-form
     (form-to
       [:DELETE action]
       (submit-button "delete"))
     (form-to
       [:POST action]
       [:input.title {:type "text" :name "title" :value (get note :title "")}]
       [:textarea.content {:name "content"} (get note :content "")]
       (submit-button "update"))]))

(defn- mod-note
  [note]
  [:div.mod-note
   [:h1
    (link-to (str "/notes/" (:ar-id note)) (:title note))]
   [:span (convert/->str (convert/->date (:ctime note)))]
   [:div.markdown (markdown/md-to-html-string (:content note))]])

(defn- mod-error
  [error]
  [:div.mod-error
   [:span error]])

(defn new-note-view
  "the page to new note"
  []
  (layout
    "new note"
    (mod-nav)
    (mod-form-note nil)))

(defn note-edit-view
  [ar-id]
  (let [note (s/fetch (->QueryNote ar-id nil nil nil))]
    (layout
      "edit note"
      (mod-nav)
      (if (or (empty? note) (= :note-deleted (:event note)))
        (mod-error "Oops, the note is not existing")
        (mod-form-note note)))))


(defn note-view
  [ar-id]
  (let [note (s/fetch (->QueryNote ar-id nil nil nil))]
    (layout
      (str (:title note))
      (mod-nav)
      (mod-note note))))
