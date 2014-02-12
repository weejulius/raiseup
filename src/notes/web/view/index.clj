(ns notes.web.view.index
  (:require [hiccup.core :refer :all]
            [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5 include-css include-js]]
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
     (include-js "/js/client.js")
     ;(include-css "http://fonts.googleapis.com/css?family=Fjalla+One")
     ]
    [:body [:div.container body]]))


(defn- footer
  []
  [:div.footer
   ])
(defn- nav
  "a nav mod including menus"
  []
  [:div.nav
   [:h1.logo
    "温故"]
   [:ul
    [:li (link-to "/notes" "Notes")]
    [:li "Problems"]]])

(defn- right-slide
  "right slides"
  []
  [:div.right-slide
   ])

(defn basic-layout
  [title modes]
  (layout title
          (nav)
          [:div.main
           [:div#mod-tips
            [:div#auto-save-tip {:class "msg"}]]
           modes]
          (right-slide)
          (footer)))


(defn- mod-notes
  [notes]
  [:div.mod-notes
   [:ul
    (for [note notes]
      [:li
       [:div.title
        [:h1
         (link-to (str "/notes/" (:ar-id note) "/form") (:title note))]
        [:span (convert/->str (convert/->date (:ctime note)))]]
       [:div.markdown
        (markdown/md-to-html-string
          (strs/head (:content note) 200
                     (str "<a class=\"more\" href=\"/notes/" (:ar-id note) "\">...More</a>")))]])]])

(defn index-view
  "the view of index"
  [{:keys [page size]}]
  (let []
    (basic-layout
      "温故知心"
      (mod-notes
        (s/fetch (->QueryNote :note nil nil page size))))))


(defn- mod-form-note
  "form to post/put note"
  [note]
  (let [new? (nil? note)
        action (str "/notes" (when-not new? (str "/" (:ar-id note))))]
    [:div.mod-note-form
     (form-to
       [:POST action]
       [:input.title {:type "text" :name "title" :value (get note :title "")}]
       [:textarea#content {:name "content"} (get note :content "")]
       [:input {:class "btn" :type :submit :value (if new? :CREATE :UPDATE)}])
     (form-to
       [:DELETE action]
       [:input {:class "btn lv2" :type :submit :value :DELETE}])
     [:input#note-id {:type :hidden :value (:ar-id note)}]

     [:script {:type "text/javascript"} "notes.web.client.run()"]]))

(defn- mod-note
  [note]
  [:div.mod-note
   [:div.title
    [:h1
     (link-to (str "/notes/" (:ar-id note)) (:title note))]
    [:span (convert/->str (convert/->date (:ctime note)))]]
   [:div.markdown (markdown/md-to-html-string (:content note))]])

(defn- mod-error
  [error]
  [:div.mod-error
   [:span error]])

(defn new-note-view
  "the page to new note"
  []
  (basic-layout
    "new note"
    (mod-form-note nil)))

(defn note-edit-view
  [ar-id]
  (let [note (s/fetch (->QueryNote :note ar-id nil nil nil))]
    (basic-layout
      "edit note"
      (if (or (empty? note) (= :note-deleted (:event note)))
        (mod-error "Oops, the note is not existing")
        (mod-form-note note)))))


(defn note-view
  [ar-id]
  (let [note (s/fetch (->QueryNote :note ar-id nil nil nil))]
    (basic-layout
      (str (:title note))
      (mod-note note))))
