(ns zjy.lol
  ^{:doc "lol api invocations"}
  (:use [clojure.test])
  (:require [clj-http.client :as client]
            [hickory.core :as hickory]
            [hickory.select :as s]))


(def lol-domain "http://lolbox.duowan.com/")
(def player-detail-url (str lol-domain "playerDetail.php"))
(def ranked-info-url (str lol-domain "ajaxGetWarzone.php"))


(defn exec-url-as-html
  "visit the url and parse the html"
  [url params]
  (let [result
        (try (client/get url params)
             (catch clojure.lang.ExceptionInfo e
               (client/get url params)))
        body (:body result)]
    (hickory/as-hickory
     (hickory/parse body))))

(defn exec-url-as-json
  [url params]
  (:body
   (client/get url (assoc params :as :json))))

(defn handle-player-detail
  [server name f]
  (f (exec-url-as-html player-detail-url
                       {:query-params {"serverName" server "playerName" name}})))




(defn find-ranked-info
  [server name]
  (exec-url-as-json ranked-info-url
                    {:query-params {"serverName" server "playerName" name}}))


(defn extract-ranked-data
  [w]
  (let [ranked-table (->
                      (s/select
                       (s/child
                        (s/class "J_content")) w)
                      first :content (nth 3)
                      :content second
                      :content second
                      :content (nth 2))
        total (-> ranked-table :content (nth 7)
                  :content first clojure.string/trim)
        win-percentage (-> ranked-table :content (nth 9) :content first)
        win (-> ranked-table :content (nth 11) :content first)
        fail (-> ranked-table :content (nth 13) :content first)
        update-date (-> ranked-table :content (nth 15) :content first)]
    [total win-percentage win fail update-date]))

(defn extract-recent-summors
  [w]
  ())

(defn extract-match-data
  [w]
  (let [ranked-table (->
                      (s/select
                       (s/child
                        (s/class "J_content")) w)
                      first :content (nth 1)
                      :content second
                      :content second
                      :content (nth 2))
        total (-> ranked-table :content (nth 3) :content first clojure.string/trim)
        win-percentage (-> ranked-table :content (nth 5) :content first)
        win (-> ranked-table :content (nth 7) :content first)
        fail (-> ranked-table :content (nth 9) :content first)
        update-date (-> ranked-table :content (nth 11) :content first)]
    [total win-percentage win fail update-date]))

(defn extract-level
  [w]
  (->
   (s/select
    (s/child
     (s/class "avatar")) w)
   first :content (nth 5) :content first))

(defn extract-from-player-detail
  [server name]
  (handle-player-detail
   server name
   (fn [w]
     (let [level (extract-level w)
           ranked (extract-ranked-data w)
           match (extract-match-data w)]
       {:level level
        :ranked ranked
        :match match}))))

(deftest test-get-player-level
  (let [detail (extract-from-player-detail "电信一" "小宝贝加油")]
    (is (= (:level detail) "30"))
    (is (= (:ranked detail) ["316" "44%" "139" "177" "10-07 16:43"]))
    (is (= (:match detail) ["561" "46.3%" "260" "301" "10-12 22:55"]))))


(deftest test-get-player-ranked
  (is (= (:tier (find-ranked-info "电信一" "小宝贝加油"))
         "黄铜")
      (= (:rank (find-ranked-info "电信一" "小宝贝加油"))
         "V")))
