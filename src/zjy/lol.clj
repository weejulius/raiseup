(ns zjy.lol
  ^{:doc "lol api invocations"}
  (:use [clojure.test])
  (:require [clj-http.client :as client]
            [hickory.core :as hickory]
            [hickory.select :as s]))


(def lol-url-domain-name "http://lolbox.duowan.com/")
(def player-detail-url (str lol-url-domain-name "playerDetail.php"))
(def ranked-info-url (str lol-url-domain-name "ajaxGetWarzone.php"))


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
  "parse the url as json"
  [url params]
  (:body
   (client/get url (assoc params :as :json))))

(defn handle-content-of-player-detail-url
  "process the content of player detail url"
  [server name f]
  (f (exec-url-as-html player-detail-url
                       {:query-params {"serverName" server "playerName" name}})))


(defn extract-from-ranked-info
  "extract required info from the ranked info url"
  [server name]
  (exec-url-as-json ranked-info-url
                    {:query-params {"serverName" server "playerName" name}}))


(defn extract-match-data-table
  "extract data from the html table structure"
  [table start]
  (if table
    (let [total (-> table :content (nth start nil)
                    :content first )
          total (if total (clojure.string/trim total) total)
          win-percentage (-> table :content (nth (+ start 2) nil) :content first)
          win (-> table :content (nth (+ start 4) nil) :content first)
          fail (-> table :content (nth (+ start 6) nil) :content first)
          update-date (-> table :content (nth (+ start 8) nil) :content first)]
      [total win-percentage win fail update-date])))

(defn extract-ranked-data
  "extract rank info from the html table"
  [w]
  (extract-match-data-table (->
                             (s/select
                              (s/child
                               (s/class "J_content")) w)
                             first :content (nth 3 nil)
                             :content second
                             :content second
                             :content (nth 2 nil))
                            7))

(defn extract-common-match-data
  [w]
  (extract-match-data-table (->
                             (s/select
                              (s/child
                               (s/class "J_content")) w)
                             first :content (nth 1 nil)
                             :content second
                             :content second
                             :content (nth 2 nil))
                            3))

(defn extract-level
  [w]
  (->
   (s/select
    (s/child
     (s/class "avatar")) w)
   first :content (nth 5) :content first))

(defn extract-from-player-detail
  [server name]
  (handle-content-of-player-detail-url
   server name
   (fn [w]
     (let [level (extract-level w)
           ranked (extract-ranked-data w)
           match (extract-common-match-data w)]
       {:level level
        :ranked ranked
        :match match}))))


(defn fetch-summor-info
  [server name]
  (let [info-from-detail (extract-from-player-detail server name)
        info-from-war-zone (extract-from-ranked-info server name)]
    (merge info-from-detail info-from-war-zone)))

(deftest test-get-player-level
  (let [detail (extract-from-player-detail "电信二" "大地爷")]
    (is (= (:level detail) "15"))
    (is (= (:ranked detail) nil))
    (is (= (:match detail) ["115" "44.3%" "51" "64" "10-02 19:35"]))))


(deftest test-get-player-ranked
  (is (= (:tier (extract-from-ranked-info "电信一" "小宝贝加油"))
         "黄铜"))
  (is (= (:rank (extract-from-ranked-info "电信一" "小宝贝加油"))
         "V"))
  (is  (= (:tier (extract-from-ranked-info "电信二" "大地爷"))
         nil)))

(deftest test-fetch-summor-info
  (is (= (:tier (fetch-summor-info "电信一" "小宝贝加油"))
         "黄铜"))
  (is (= (:rank (fetch-summor-info "电信一" "小宝贝加油"))
         "V")))
