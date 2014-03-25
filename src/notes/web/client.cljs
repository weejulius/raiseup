(ns notes.web.client
  (:require
    [goog.events :as events]
    [ajax.core :refer [POST GET ajax-request raw-response-format]]
    [dommy.core :as dom]
    [om.core :as om :include-macros true]
    [om.dom :as d :include-macros true]
    [goog.storage.mechanism.HTML5SessionStorage :as html5ss]
    [markdown.core :as md]
    [cljs.core.async :refer [put! >! <! alts! chan take!] :as asyn]
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


(def history
  "browser history instance"
  (Html5History.))

(.setUseFragment history false)
(.setPathPrefix history "/demo")
(.setEnabled history true)


;; access via uri------------------------
;;                                       |
;; shortcut -> name -> uri -> access uri resource -> render resource

;; go back
;;
;;
(def res (atom nil))

(defn- on-resp
  [ch [ok response]]
  (when-let [resp (reader/read-string response)]
    (go (>! ch resp))))




;;问题： resource可以有多个方式访问，通过快捷键命令，或者直接uri访问，如果是前者可以直接访问之前的content，减少请求
;;问题: 监听body的keyup事件会导致输入note有冲突
;;
;; 命令 URI 获取展示数据 执行Action

(defprotocol Resource
  (desc [this] "get the description")
  (shortcut [this] "get the shortcut")
  (match-uri [this s] "match the uri")
  (gen-uri-for-cmd [this data] "generate the uri for the command")
  (avail-resources [this] "the available resources of this resource ")
  (pre-data-for-cmd [this data-ch data params] "make data")
  (pre-data-for-uri [this data-ch params] "make data")
  (render-view [this data]))



(defn- render-notes
  [data]
  (if (seq? data)
    (apply d/ul #js {:className "mod-notes"}
           (map
             #(d/li nil
                    (d/h1 #js {:className "title"} (:title %))
                    (d/div #js {:className "additional"}
                           (d/span nil (as-date-str (:ctime %)))
                           (d/span nil (:author %))))
             data))))

(defn- render-note
  [data]
  (let [note data]
    (d/div #js {:className "mod-note"}
           (d/div nil
                  (d/h1 #js {:className "title"} (:title note))
                  (d/div #js {:className "additional"}
                         (d/span nil (as-date-str (:ctime note)))
                         (d/span nil (:author note))))
           (d/div #js {:className "markdown"}
                  (md/mdToHtml (:content note))))))


(defrecord Note []
  Resource
  (desc [this]
    "open the note")
  (shortcut [this]
    :o)
  (match-uri [this s]
    (re-matches #"/(\d)+" s))
  (gen-uri-for-cmd [this data]
    (str "/" (:ar-id data)))
  (avail-resources [this]
    [])
  (pre-data-for-cmd [this data-ch data [num]]
    (go (put! data-ch (nth data (dec (long num)) 0))))
  (pre-data-for-uri [this data-ch [num]]
    (ajax-request "/notes/cmd" :get
                  {:params  {:cmd :note :ar-id num}
                   :handler (partial on-resp data-ch)
                   :format  (raw-response-format)}))
  (render-view [this data]
    (render-note data)))



(defrecord Recent []
  Resource
  (desc [this]
    "get the recent note")
  (shortcut [this]
    :recent)
  (match-uri [this s]
    (= "/recent" s))
  (gen-uri-for-cmd [this data]
    "/recent")
  (avail-resources [this]
    [[:o :open-note]])
  (pre-data-for-cmd [this data-ch data _]
    (let []
      (ajax-request "/notes/cmd" :get
                    {:params  {:cmd :recent}
                     :handler (partial on-resp data-ch)
                     :format  (raw-response-format)})))
  (pre-data-for-uri [this data-ch _]
    (pre-data-for-cmd this nil _))
  (render-view [this data]
    (render-notes data)))




(def resources [(->Recent) (->Note)])

(def input-state (atom []))

(def app-state (atom {:resource     nil
                      :available-rs []
                      :data         nil
                      :uri          ""}))



;; input -> parse input -> find resource -> retrieve content -> send content to channel -> get content and update state
;;  -> render
;;
;; input render-fn content available-rs

;;  next-available-commands
;; history
;; change url

(def keymap
  {:enter 13})

(defn key-is?
  [keycode key]
  (= keycode (key keymap)))


(defn- parse-shortcut
  [input-state]
  (if-let [inputs @input-state]
    (let [shortcut (seq (.split (apply str inputs) " "))
          shortcut-name (first shortcut)
          name-kw (keyword (.toLowerCase shortcut-name))]
      [name-kw (rest shortcut)])))

(defn- parse-inputs
  "take the user's comming input and parse to command when enter is comming"
  [keycode input-state]
  (let [char (.fromCharCode js/String keycode)]
    (if (key-is? keycode :enter)
      (let [shortcut (parse-shortcut input-state)]
        (om/update! input-state [])
        shortcut)
      (do
        (om/transact! input-state (fn [input]
                                    (conj input char)))
        nil))))

(def histories (atom []))
(def channel (chan))
(def data-ch (chan))



(defn- pop-resource-from-history
  [histories]
  (let [rs (second @histories)]
    (log "his" rs @histories)
    (reset! histories (rest @histories))
    rs))

(defn back-command?
  [shortcut-kw]
  (= :b shortcut-kw))

(defn- prepare-resource-data
  [app-state shortcut-kw params]
  )



(defn input-component
  [input-state owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (dom/listen! (sel1 :body)
                   :keyup
                   (fn [event]
                     (when-let [shortcut (parse-inputs (.-keyCode event) input-state)]
                       (put! channel shortcut)))))
    om/IRenderState
    (render-state [_ _]
      (d/span #js {:id "cmd"}
              (apply str input-state)))))


(defn app-component
  [{:keys [available-rs data uri] :as app-state} owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go (while true
            (when-let [[shortcut-kw params] (<! channel)]
              (when-let [matched-resource (if (back-command? shortcut-kw)
                                            (pop-resource-from-history histories)
                                            (when-let [resource (some #(if (= shortcut-kw (shortcut %)) %) resources)]
                                              (pre-data-for-cmd resource data-ch (:data @app-state) params)
                                              (let [[data _] (alts! [data-ch (asyn/timeout 1000)])]
                                                {:shortcut     shortcut-kw
                                                 :data         data
                                                 :uri          (gen-uri-for-cmd resource data)
                                                 :available-rs (avail-resources resource)})))]
                (om/update! app-state matched-resource)
                (. history (setToken (:uri matched-resource) ""))
                (if-not (back-command? shortcut-kw)
                  (swap! histories #(cons matched-resource %))))))))
    om/IRenderState
    (render-state [_ _]
      (d/div
        nil
        (apply d/ul #js {:id "next-behaviors"}
               (map (fn [avail-resource]
                      (d/li nil
                            (d/span nil (str (first avail-resource)))
                            (d/span nil (str (second avail-resource)))))
                    available-rs))
        (d/div #js {:id "content"}
               (when data
                 (render-view (some #(if (= (:shortcut app-state) (shortcut %)) %) resources) data)))))))



(defn demo-ready
  []
  (om/root input-component input-state {:target (sel1 :#cmd-box)})
  (om/root app-component app-state {:target (sel1 :#app)}))

#_(events/listen history EventType.NAVIGATE
               (fn [e] (secretary/dispatch! (.-token e))))




