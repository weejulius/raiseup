(ns zjy.web
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [system :as s]
            [zjy.command :refer :all]
            [zjy.view :as v]
            [clojure.edn :as edn]))


(defroutes zjy-routes
  (GET "/cmds" []
       (fn [req]
         (str (s/send-command
               (edn/read-string (-> req :params :command))))))
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
