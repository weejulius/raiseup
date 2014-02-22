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
  [authed?]
  [:div.nav
   [:h1.logo
    "温故"]
   [:ul#menu
    [:li (link-to "/notes" "Notes")]
    [:li "Problems"]]
   (if authed?
     [:ul#auth
      [:li#logout
       [:a {:class "" :href "/notes/users/logout"} "退出"]]]

     [:ul#auth
      [:li#login
       [:a {:class ""} "登陆"]
       (form-to
         [:POST "/notes/users/login"]
         [:input {:type :text :name :name :placeholder "输入用户名"}]
         [:input {:type :password :name :password :placeholder "输入密码"}]
         [:input {:type :submit :value "确定"}])]
      [:li#reg
       [:a {:class ""} "注册"]
       (form-to
         [:POST "/notes/users"]

         [:input {:type :text :name :name :placeholder "输入用户名"}]
         [:input {:type :password :name :password :placeholder "输入密码"}]
         [:input {:type :submit :value "确定"}])]])
   [:script {:type "text/javascript"}
    "notes.web.client.nav_ready();"]])

(defn- right-slide
  "right slides"
  [logined-user-name]
  [:div.right-slide
   [:div#current-user
    (if-not (empty? logined-user-name)
      [:span [:a {:href (str "/notes/" logined-user-name)} logined-user-name] "的空间"])]])

(defn basic-layout
  [logined-user-name title modes]
  (layout title
          (nav (not (empty? logined-user-name)))
          [:div.main
           [:div#mod-tips
            [:div#auto-save-tip {:class "msg"}]]
           modes]
          (right-slide logined-user-name)
          (footer)))


(defn- mod-notes
  [notes editable?]
  [:div.mod-notes
   (if editable?
     [:a {:class "btn lv1" :href "/notes/new"} "新建"])
   [:ul
    (for [note notes]
      [:li
       [:div.title
        [:h1
         (link-to (str "/notes/" (:ar-id note) (if editable? "/form")) (:title note))]
        [:div.additional
         [:span (convert/->str (convert/->date (:ctime note)))]
         [:span [:a {:href (str "/notes/" (:author note))} (:author note)]]]]
       (let [max-length-words 200
             content (:content note)
             only-show-summary? (< max-length-words (.length content))
             content (if only-show-summary? (subs content 0 max-length-words) content)]
         [:div.markdown
          (markdown/md-to-html-string content)
          (if only-show-summary?
            [:a.more {:href (str "/notes/" (:ar-id note))} "...More "])])])]])




(defn- mod-form-note
  "form to post/put note"
  [note]
  (let [new? (nil? note)
        action (str "/notes" (when-not new? (str "/" (:ar-id note))))]
    [:div
     [:div.mod-note-form
      (form-to
        [:POST action]
        [:input.title {:type "text" :name "title" :value (get note :title "")}]
        [:textarea#content {:name "content"} (get note :content "")]
        [:input {:class "btn lv1" :type :submit :value (if new? :CREATE :UPDATE)}])
      (form-to
        [:DELETE action]
        [:input {:class "btn lv1 lv2" :type :submit :value :DELETE}])]
     [:div#preview {:class "markdown"}]
     [:input#note-id {:type :hidden :value (:ar-id note)}]
     [:script {:type "text/javascript"}
      "notes.web.client.note_form_ready();"]]))



(defn- mod-note
  [note]
  [:div.mod-note
   [:div.title
    [:h1
     (link-to (str "/notes/" (:ar-id note)) (:title note))]
    [:div.additional
     [:span (convert/->str (convert/->date (:ctime note)))]
     [:span (:author note)]]]
   [:div.markdown (markdown/md-to-html-string (:content note))]])

(defn- mod-error
  [error]
  [:div.mod-error
   [:span error]])

(defn load-index-view
  "the view of index"
  [user-name notes]
  (let []
    (basic-layout
      user-name
      "温故知心"
      (mod-notes
        notes
        false))))

(defn new-note-view
  "the page to new note"
  [logined-user-name]
  (basic-layout
    logined-user-name
    "new note"
    (mod-form-note nil)))

(defn load-note-edit-view
  [logined-user-name note]
  (basic-layout
    logined-user-name
    (:title note)
    (mod-form-note note)))


(defn load-note-view
  [logined-user-name ar-id]
  (let [note (s/fetch (->QueryNote :note ar-id nil nil nil))]
    (basic-layout
      logined-user-name
      (str (:title note))
      (mod-note note))))


(defn load-user-notes-view
  [notes logined-user-name editable?]
  (basic-layout
    logined-user-name
    (str logined-user-name "的札记")
    (mod-notes notes editable?)))