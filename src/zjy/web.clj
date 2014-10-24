(ns zjy.web
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [system :as s]
            [zjy.command :refer :all]
            [zjy.view :as v]
            [zjy.lol :as lol]
            [clojure.edn :as edn]))


(defroutes zjy-routes
  (GET "/cmds" []
       (fn [req]
         (str (s/send-command
               (edn/read-string (-> req :params :command))))))
  (GET "/q" []
       (fn [req]
         (let [game-server  (-> req :params :game_server)
               game-name  (-> req :params :game_name)]
           (str (lol/fetch-summor-info game-server game-name)))))
  (GET "/" []
       (fn [req]
         (v/index))))




#_(s/send-command {:command :active-account
                   :ar :account
                   :game-name "小宝贝加油"
                   :game-server "电信一"
                   :dirty-words :hate
                   :age :little
                   :password "12312123213123"
                   :hashed-password "12312312"
                   :ctime 1231312312312})
