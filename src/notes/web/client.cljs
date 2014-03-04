(ns notes.web.client
  (:require
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
    [dommy.macros :only [node sel sel1]]))

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

(defn def-command
  [name desc shortcut possible-behaviors handler]
  (swap! name-map-def #(assoc % name {:fn        handler
                                      :desc      desc
                                      :behaviors possible-behaviors}))
  (swap! shortcut-map-name #(assoc % shortcut name))
  (swap! name-map-shortcut #(assoc % name shortcut)))


(defn- find-event-map
  [s]
  (if-let [f-name (s @shortcut-map-name)]
    (f-name @name-map-def)))

(defn- on-recent-resp
  [comm [ok response]]
  (let [cmd-box (sel1 :#cmd-box)
        resp (sel1 :#resp)
        res (reader/read-string response)]
    (put! comm [:notes-fetched res])))

(def-command
  :recent
  "list recent notes"
  :recent
  [:open-note]
  (fn [comm]
    (ajax-request "/notes/cmd" :get
                  {:params  {:cmd :recent}
                   :handler (partial on-recent-resp comm)
                   :format  (raw-response-format)})))


(def-command
  :open-note
  "open note"
  :o
  [:back]
  (fn [comm num]
    (put! comm [:open-note (long num)])))


(def app-state (atom {:input     []
                      :content   []
                      :next      []
                      :render-fn nil
                      :error-msg ""}))


(def keymap
  {:enter 13})

(defn key-is?
  [keycode key]
  (= keycode (key keymap)))

(defn listen-input
  "watching the user's input and make action"
  [app-state keycode comm]
  (let [char (.fromCharCode js/String keycode)]
    (if (key-is? keycode :enter)
      (let [input-str (apply str (:input @app-state))
            splits (seq (.split input-str " "))
            key (keyword (.toLowerCase (first splits)))
            event-map (find-event-map key)
            handler (:fn event-map)]
        (om/update! app-state :input [])
        (om/update! app-state :next (map
                                      (fn [behavior]
                                        (if-let [shortcut (behavior @name-map-shortcut)]
                                          [(name shortcut) (name behavior)]))
                                      (:behaviors event-map)))
        (log app-state)
        (if-not (nil? handler)
          (apply handler comm (rest splits))))
      (om/transact! app-state :input
                    (fn [input] (conj input char))))))

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
                  (md/mdToHtml (:content note)
                               :code-style #(str "class=\"" % "\""))))))


(defn handle-input-command
  "handle the input command from channel"
  [type app val comm]
  (case type
    :input-char (listen-input app val comm)
    :notes-fetched (do
                     (om/update! app :content val)
                     (om/update! app :render-fn render-notes))
    :open-note (let []
                 (om/transact! app :content (fn [content] (nth content (dec val))))
                 (om/update! app :render-fn render-note))
    nil))




(defn app
  [app-state owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [comm (chan)]
        (om/set-state! owner :comm comm)
        (go (while true
              (let [[type value] (<! comm)]
                (handle-input-command type app-state value comm))))
        (dom/listen! (sel1 :body)
                     :keyup
                     (fn [event]
                       (put! comm [:input-char (.-keyCode event)])))))
    om/IRenderState
    (render-state [_ _]
      (d/div
        nil
        (d/span #js {:id "cmd"} (apply str (:input app-state)))
        (apply d/ul #js {:id "next-behaviors"}
               (map #(d/li nil
                           (d/span nil (str (first %) ": "))
                           (d/span nil (second %)))
                    (:next app-state)))
        (d/div #js {:id "content"}
               (if-let [render-fn (:render-fn app-state)]
                 (render-fn app-state)))))))



#_(defn blink-cursor
  []
  (js/setInterval #(dom/toggle-class! (sel1 :#cursor) :blink) 600))


(defn demo-ready
  []
  (om/root app app-state {:target (sel1 :#app)}))

