(ns notes.web.client
  (:require
    [ajax.core :refer [POST GET ajax-request raw-response-format]]
    [dommy.core :as dom]
    [om.core :as om :include-macros true]
    [om.dom :as d :include-macros true]
    [goog.storage.mechanism.HTML5SessionStorage :as html5ss]
    [markdown.core :as md]
    [cljs.reader :as reader])
  (:use-macros
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


(defn cmpt-notes
  [notes owner]
  (reify
    om/IRender
    (render [this]
      (apply d/ul #js {:className "mod-notes"}
             (vec
               (map
                 #(d/li nil
                        (d/h1 #js {:className "title"} (:title %))
                        (d/div #js {:className "additional"}
                               (d/span nil (as-date-str (:ctime %)))
                               (d/span nil (:author %))))
                 notes))))))


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

(def kw-map-fn (atom {}))
(def shotcus-map (atom {}))
(def req-res (atom []))

(defn def-command
  [name shotcuts possible-behaviors handler]
  (swap! kw-map-fn #(assoc % name {:fn        handler
                                   :behaviors possible-behaviors}))
  (swap! shotcus-map #(assoc % shotcuts name)))


(defn- find-fn-by-shotcut
  [s]
  (if-let [f-name (s @shotcus-map)]
    (get-in @kw-map-fn [f-name :fn])))

(defn- resp-send-command
  [[ok response]]
  (let [cmd-box (sel1 :#cmd-box)
        resp (sel1 :#resp)
        res (reader/read-string response)]
    (log res)
    (reset! req-res res)
    (dom/set-style! cmd-box :top "30px")))

(def-command
  :recent
  :recent
  [:open-note :peep-note :next-page :prev-page]
  (fn []
    (ajax-request "/notes/cmd" :get
                  {:params  {:cmd :recent}
                   :handler resp-send-command
                   :format  (raw-response-format)})))

(def input (atom []))

(def keymap
  {:enter 13})

(defn key-is?
  [keycode key]
  (= keycode (key keymap)))

(defn listen-commands
  []
  (dom/listen! (sel1 :body)
               :keyup
               (fn [event]
                 (let [keycode (.-keyCode event)
                       char (.fromCharCode js/String keycode)]
                   (if (key-is? keycode :enter)
                     (let [input-str (apply str @input)
                           splits (seq (.split input-str " "))
                           key (keyword (.toLowerCase (first splits)))
                           handler (find-fn-by-shotcut key)]
                       (log input-str key handler (rest splits))
                       (reset! input [])
                       (if-not (nil? handler)
                         (apply handler (rest splits))))
                     (swap! input conj char))))))


(defn cmpt-cmd
  [cmd owner]
  (reify
    om/IRender
    (render [this]
      (d/span #js {:id "cmd"} (apply str cmd)))))



#_(defn blink-cursor
  []
  (js/setInterval #(dom/toggle-class! (sel1 :#cursor) :blink) 600))


(def app-state (atom {}))

(defn demo-ready
  []
  (om/root cmpt-notes req-res
           {:target (sel1 :#resp)})
  (om/root cmpt-cmd input
           {:target (sel1 :#cmd-box)})
  (listen-commands)
  #_(blink-cursor))

