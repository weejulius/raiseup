(ns notes.web.client
  (:require
    [goog.events :as events]
    [ajax.core :refer [POST GET ajax-request raw-response-format]]
    [dommy.core :as dom]
    [om.core :as om :include-macros true]
    [om.dom :as d :include-macros true]
    [goog.storage.mechanism.HTML5SessionStorage :as html5ss]
    [markdown.core :as md]
    [cljs.core.async :refer [put! <! chan]]
    [cljs.reader :as reader]
    )
  (:use-macros
    [cljs.core.async.macros :only [go]]
    [dommy.macros :only [node sel sel1]])
  (:import [goog.history Html5History]))

(defn- now-as-mills
  []
  (.getTime (js/Date.)))

(defn- val-for-el
  [el]
  (.-value (sel1 el)))

(defn- log
  [& msg]
  (.log js/console (apply str msg)))



(def storage
  (goog.storage.mechanism.HTML5SessionStorage.))

(def auto-save-time-state (atom (js/Date.)))

(def check-auto-save-interval 2000)


(defn- resp-save-note-command
  [response]
  (reset! auto-save-time-state (js/Date.)))

(defn- send-update-note-command
  []
  (POST "/notes/commands"
        {:params   {"command" (str {:ar      :note
                                    :command :update-note
                                    :utime   (now-as-mills)
                                    :title   (val-for-el :.title)
                                    :content (val-for-el :#content)
                                    :ar-id   (int (val-for-el :#note-id))})}
         :handler  resp-save-note-command
         :format   :raw
         :keyword? true}))


(defn- save-note-to-session-storage
  [session-storage key note]
  (.set session-storage key note)
  (reset! auto-save-time-state (js/Date.)))

(defn- remove-note-from-session-storage
  [session-storage key]
  (.remove session-storage key))

(defn- storage-key
  []
  (str "note" (val-for-el :#note-id)))

(defn- save-note-local
  []
  (save-note-to-session-storage
    storage
    (storage-key)
    (val-for-el :#content)))

(defn- auto-save-note
  []
  (js/setTimeout
    #(save-note-local) 3000))

(defn- clear-note-local-storage
  []
  (doall
    (map (fn [el]
           (dom/listen! el :click
                        (fn [evt]
                          (remove-note-from-session-storage storage (storage-key)))))
         (sel :.act))))



(defn- pull-content-from-local-by-key
  "if the note is crashed when editing, pulling from local"
  [key]
  (let [content (.get storage key)]
    (if-not (empty? content)
      (dom/set-text! (sel1 :#content) content)
      (.set storage (str (storage-key) "-his") (val-for-el :#content)))))

(defn- pull-content-from-local
  "if the note is crashed when editing, pulling from local"
  []
  (pull-content-from-local-by-key (storage-key)))

(defn- listen-save-note-key-press
  []
  (dom/listen! (sel1 :body)
               :keyup
               (fn [event]
                 (let [keycode (.-keyCode event)
                       shift-key (.-shiftKey event)
                       ctrl-key (.-ctrlKey event)
                       save-note? (and shift-key ctrl-key (= keycode 83))]
                   (if save-note?
                     (send-update-note-command))))))

(defn discard-local-changes
  []
  (dom/listen! (sel1 :.discard)
               :click
               (fn [event]
                 (remove-note-from-session-storage storage (storage-key))
                 (dom/set-value!
                   (sel1 :#content)
                   (.get storage (str (storage-key) "-his"))))))

(defn- update-auto-save-note-tip
  []
  (auto-save-note)
  ;(discard-local-changes)
  [:span (str "Tip: note is saved to local at " @auto-save-time-state)
   [:a.discard "Discard"]])

(defn preview-on-content-change
  []
  (dom/listen! (sel1 :#content)
               :keyup
               (fn [event]
                 (dom/set-html! (sel1 :#preview)
                                (md/mdToHtml (val-for-el :#content)
                                             :code-style #(str "class=\"" % "\""))))))



(defn click-reg-user
  []
  (dom/listen! (sel1 [:#reg :a])
               :click
               (fn [event]
                 (dom/toggle-class! (sel1 [:#reg :form]) :show))))


(defn click-login-user
  []
  (dom/listen! (sel1 [:#login :a])
               :click
               (fn [event]
                 (dom/toggle-class! (sel1 [:#login :form]) :show))))

(defn ^:export run []
  )


(defn note-form-ready
  []
  (run)
  (pull-content-from-local)
  (clear-note-local-storage)
  (listen-save-note-key-press)
  (preview-on-content-change)
  (discard-local-changes))

(defn nav-ready
  []
  (click-reg-user)
  (click-login-user))



;;;;;;;;;;;;;;;;;;;------------------------demo-------------------------------------

(defn- as-date-str
  [^long m]
  (.toLocaleDateString
    (doto (js/Date.)
      (.setTime m))))





#_{:o "open"
 :pp "peep"
 :pv "preview"
 :n "next page"
 :p "previous page"

 :b "back"
 :e "edit"
 :l "left"
 :r "right"
 :um "user's more"
 :. "repeat"
 :c "comment"
 :lc "list comment"
 :enter "confirm,submit"}


(def resource-name-map-config
  "the name of command and the map with its config"
  (atom {}))


(def shortcut-map-resource-name
  "shortcut map to command name"
  (atom {}))

(def resource-name-map-shortcut
  "command name map to shortcut"
  (atom {}))


(def histories
  "the access histories, used by the back navigation"
  (atom []))


(def render-fn
  "the current render fn of view"
  (atom nil))

(def history
  "browser history instance"
  (Html5History.))

(.setUseFragment history false)
(.setPathPrefix history "/demo")
(.setEnabled history true)

(def uri-matchers
  "matchers for the uri"
  (atom {}))

;; access via uri------------------------
;;                                       |
;; shortcut -> name -> uri -> access uri resource -> render resource

;; go back
;;
;;

(defn def-resource
  "define the resource and its config"
  [name desc shortcut available-rs match-uri uri-gen-fn handle-resource]
  (swap! resource-name-map-config #(assoc % name {:fn           handle-resource
                                                  :uri-fn       uri-gen-fn
                                                  :desc         desc
                                                  :available-rs available-rs}))
  (if match-uri
    (swap! uri-matchers name match-uri))
  (swap! shortcut-map-resource-name #(assoc % shortcut name))
  (swap! resource-name-map-shortcut #(assoc % name shortcut)))


(defn- find-resource-config-by-shortcut
  [s]
  (if-let [f-name (s @shortcut-map-resource-name)]
    (f-name @resource-name-map-config)))

(defn- on-recent-resp
  [channel [ok response]]
  (if-let [res (reader/read-string response)]
    (put! channel [:notes res])))

(def-resource
  :recent
  "list recent notes"
  :recent
  [:open-note]
  (fn [uri]
    (if (= "/recent" uri)
      ""))
  (fn [] "/recent")
  (fn []
    (fn [channel]
      (let [cmd (ajax-request "/notes/cmd" :get
                              {:params  {:cmd :recent}
                               :handler (partial on-recent-resp channel)
                               :format  (raw-response-format)})]))))

(def-resource
  :open-note
  "open note"
  :o
  []
  (fn [uri]
    (if-let [matches (re-matches #"/(\d)+" uri)]
      (second matches)))
  (fn [num]
    (str "/" num))
  (fn [num]
    [:note (long num)]))


(def-resource
  :back
  "back"
  :b
  []
  nil
  nil
  (fn []
    (let [command (vec (rest (first @histories)))
           c (vec (conj command true))]
      (log "back" c command)
      c)))


(def app-state (atom {:input        []
                      :content      []
                      :available-rs []
                      :error-msg    ""}))



;; input -> command -> handle-command --(event)--> push-event -> handle-event -> update-state -> render-view
;;
;; next-available-commands
;; history
;; change url

(def keymap
  {:enter 13})

(defn key-is?
  [keycode key]
  (= keycode (key keymap)))


(defn- parse-shortcut
  [app-state]
  (if-let [inputs (:input @app-state)]
    (let [command (seq (.split (apply str inputs) " "))
          name (first command)
          name-kw (keyword (.toLowerCase name))]
      [name-kw (rest command)])))

(defn- parse-inputs
  "take the user's comming input and parse to command when enter is comming"
  [keycode app-state]
  (let [char (.fromCharCode js/String keycode)]
    (if (key-is? keycode :enter)
      (let [shortcut (parse-shortcut app-state)]

        shortcut)
      (do
        (om/transact! app-state :input
                      (fn [input]
                        (conj input char)))
        nil))))


(defn- parse-shortcut-to-resource-params
  [shortcut]
  (log shortcut)
  (when-let [[shortcut-name params] shortcut]
    (if-let [resource-name (shortcut-name @shortcut-map-resource-name)]
      [resource-name params])))

(defn- parse-uri-to-resource-params
  [uri]
  (loop [matchers @uri-matchers]
    (if-let [[resource-name match-uri] (first matchers)]
      (if-let [params (match-uri uri)]
        [resource-name params]
        (recur (rest matchers))))))

(defn- handle-resource-params
  [resource-params]
  (log "handle resource param" resource-params)
  (if resource-params
    (if-let [[name params] resource-params]
      (if-let [resource-config (name @resource-name-map-config)]
        (if-let [handle-resource (:fn resource-config)]
          (apply handle-resource params))))))

(defn- push-resource-to-channel
  [resource]
  (fn [channel]
    (if resource
      (if (vector? resource) ;;if it is [key v] we send it to channel, or it send by itself
        (put! channel resource)
        (resource channel)))))


(defn- render-notes
  [app-state]
  (apply d/ul #js {:className "mod-notes"}
         (map
           #(d/li nil
                  (d/h1 #js {:className "title"} (:title %))
                  (d/div #js {:className "additional"}
                         (d/span nil (as-date-str (:ctime %)))
                         (d/span nil (:author %))))
           (:content app-state))))


(defn- render-note
  [app-state]
  (let [note (:content app-state)]
    (d/div #js {:className "mod-note"}
           (d/div nil
                  (d/h1 #js {:className "title"} (:title note))
                  (d/div #js {:className "additional"}
                         (d/span nil (as-date-str (:ctime note)))
                         (d/span nil (:author note))))
           (d/div #js {:className "markdown"}
                  (md/mdToHtml (:content note))))))

(defn- update-available-commands
  "update the available commands, having both shortcut and command name"
  [app-state short-name]
  (log "update " short-name)
  (om/update! app-state :available-rs
              (map
                (fn [available-cmd]
                  (if-let [shortcut (available-cmd @resource-name-map-shortcut)]
                    [(name shortcut) (:desc (available-cmd @resource-name-map-config))]))
                (:available-rs (find-resource-config-by-shortcut short-name)))))


(defn- manage-history-stack
  [uri title app-state type value back?]
  (. history (setToken uri title))
  (if-not back?
    (let [shortcut (parse-shortcut app-state)
          shortcut-name (first shortcut)]
      (swap! histories
             (fn [h]
               (cons [shortcut-name type value] h)))
      (update-available-commands app-state shortcut-name)
      (om/update! app-state :input []))
    (do
      (update-available-commands app-state (ffirst @histories))
      (swap! histories (comp vec rest))
      (om/update! app-state :input []))))



(defn handle-event
  "handle event and update state"
  [type app val back?]
  (log "..." type app val)
  (case type
    :notes (do
             (om/update! app :content val)
             (reset! render-fn render-notes)
             (manage-history-stack "" "notes" app type val back?))

    :note (let []
            (log "val" val (:content @app))
            (om/transact! app :content (fn [content] (nth content (dec (long val)))))
            (reset! render-fn render-note)
            (manage-history-stack (str "/" (:ar-id (:content @app))) "notes" app type val back?))
    nil))




(defn app
  [app-state owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [channel (chan)]
        (om/set-state! owner :channel channel)
        (go (while true
              (let [[type value back?] (<! channel)]
                (handle-event type app-state value back?))))
        (dom/listen! (sel1 :body)
                     :keyup
                     (fn [event]
                       ((push-resource-to-channel
                          (handle-resource-params
                            (parse-shortcut-to-resource-params
                              (parse-inputs
                                (.-keyCode event)
                                app-state))))
                        channel)))))
    om/IRenderState
    (render-state [_ _]
      (d/div
        nil
        (d/span #js {:id "cmd"}
                (apply str (:input app-state)))
        (apply d/ul #js {:id "next-behaviors"}
               (map #(d/li nil
                           (d/span nil (str (first %) ": "))
                           (d/span nil (second %)))
                    (:available-rs app-state)))
        (d/div #js {:id "content"}
               (when-let [render @render-fn]
                 (render app-state)))))))



(defn demo-ready
  []
  (om/root app app-state {:target (sel1 :#app)}))




#_(defn blink-cursor
  []
  (js/setInterval #(dom/toggle-class! (sel1 :#cursor) :blink) 600))

