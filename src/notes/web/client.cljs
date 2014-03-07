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
    [cljs.reader :as reader])
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
(def commands
  {:recent [:o :pp :n :p :b]
   :o      [:e :b :r :l :um :c :lc]
   :e      [:pv :b :enter]
   :pv     [:b]
   :pp     [:b]
   :um     [:. :b :o]
   :c      [:b :enter :e]
   :lc     [:n :p]
   })

(def name-map-def (atom {}))
(def shortcut-map-name (atom {}))
(def name-map-shortcut (atom {}))
(def histories (atom []))
(def history (Html5History.))

(.setUseFragment history false)
(.setPathPrefix history "/demo")
(.setEnabled history true)

(defn def-command
  [name desc shortcut possible-behaviors handler]
  (swap! name-map-def #(assoc % name {:fn            handler
                                      :desc          desc
                                      :available-cmd possible-behaviors}))
  (swap! shortcut-map-name #(assoc % shortcut name))
  (swap! name-map-shortcut #(assoc % name shortcut)))


(defn- find-command-setting
  [s]
  (if-let [f-name (s @shortcut-map-name)]
    (f-name @name-map-def)))

(defn- on-recent-resp
  [channel [ok response]]
  (if-let [res (reader/read-string response)]
    (put! channel [:notes-fetched res])))

(def-command
  :recent
  "list recent notes"
  :recent
  [:open-note]
  (fn []
    (fn [channel]
      (let [cmd (ajax-request "/notes/cmd" :get
                              {:params  {:cmd :recent}
                               :handler (partial on-recent-resp channel)
                               :format  (raw-response-format)})]))))

(def-command
  :open-note
  "open note"
  :o
  [:back]
  (fn [num]
    [:open-note (long num)]))


(def-command
  :back
  "back"
  :b
  []
  (fn []
    (let [command (first @histories)]
      (swap! histories (comp vec rest))
      command)))


(def app-state (atom {:input         []
                      :content       []
                      :available-cmd []
                      :render-fn     nil
                      :error-msg     ""}))



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

(defn- parse-input
  "take the user's comming input and parse to command when enter is comming"
  [keycode app-state]
  (let [char (.fromCharCode js/String keycode)]
    (if (key-is? keycode :enter)
      (parse-shortcut app-state)
      char)))



(defn- update-available-commands
  "update the available commands, having both shortcut and command name"
  [app-state command-name]
  (om/update! app-state :available-cmd
              (map
                (fn [behavior]
                  (if-let [shortcut (behavior @name-map-shortcut)]
                    [(name shortcut) (name behavior)]))
                (:available-cmd (find-command-setting command-name)))))

(defn- handle-command-by-shortcut
  [command]
  (if (vector? command) ;;is command or just char
    (if-let [[name params] command]
      (let [command-setting (find-command-setting name)
            handle-command (:fn command-setting)]
        (if handle-command
          (apply handle-command params))))
    [:char-input command]))

(defn- push-event-to-channel
  [event]
  (fn [channel]
    (if event
      (if (vector? event)
        (put! channel event)
        (event channel)))))


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



(defn- push-to-history-stack
  [uri title app-state type value]
  (. history (setToken uri title))
  (swap! histories conj [type value])
  (update-available-commands app-state
                             (first (parse-shortcut app-state))))

(defn handle-event
  "handle event and update state"
  [type app val]
  (case type
    :char-input (let []
                  (om/transact! app :input
                                (fn [input] (conj input val))))
    :notes-fetched (do
                     (om/update! app :content val)
                     (om/update! app :render-fn render-notes)
                     (push-to-history-stack "/notes" "notes" app type val))
    :note-opened (let []
                   (om/transact! app :content (fn [content] (nth content (dec val))))
                   (om/update! app :render-fn render-note)
                   (push-to-history-stack (str "/notes" (:ar-id (:content app))) "notes" app type val))
    nil))




(defn app
  [app-state owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [channel (chan)]
        (om/set-state! owner :channel channel)
        (go (while true
              (let [[type value] (<! channel)]
                (handle-event type app-state value))))
        (dom/listen! (sel1 :body)
                     :keyup
                     (fn [event]
                       ((push-event-to-channel
                          (handle-command-by-shortcut
                            (parse-input
                              (.-keyCode event)
                              app-state)))
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
                    (:available-cmd app-state)))
        (d/div #js {:id "content"}
               (when-let [render-fn (:render-fn app-state)]
                 (render-fn app-state)))))
    om/IDidUpdate
    (did-update [_ _ _]
      ; (om/update! app-state :input [])
      ;(om/update! app-state :render-fn [])
      )))



(defn demo-ready
  []
  (om/root app app-state {:target (sel1 :#app)}))




#_(defn blink-cursor
  []
  (js/setInterval #(dom/toggle-class! (sel1 :#cursor) :blink) 600))

