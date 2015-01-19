(ns zjy.domain
  ^{:doc "domain in the zhao ji you"}
  (:require [schema.core :as s]
            [cqrs.core :as c]))

(defn create-room
  ^{:doc "create a new room, meanwhile the questions,filters and duration are filled with"}
  [])

(defn exit-room
  ^{:doc "exit a room"}
  [])

(defn join-room
  ^{:doc "join a room, it will qualifier the member to join,
          the join is rejected if not qualified"}
  [])

(defn invite-member
  [])

(defn active-account
  ^{:doc "active an account by the game id"}
  [ar cmd]
  (c/gen-event :account-activated cmd
               [:game-name :game-server :dirty-words :age :hashed-password :ctime]))

(defn update-account
  ^{:doc "update the account, the info of member includes:
          1. roles with level
          2. positions are good at
          3. credit owned
          4. tags owned
          5. history of match by zjy
          "
    }
 []
 1)



;(s/defrecord GameInfo [level :- s/Int                ;;等级
   ;                    rank-match-level :- s/Int　　 ;;排位等级,例如黄铜
   ;                    rank-match-level-num :- s/Int ;;排位级别,例如V
   ;                    common-match-times :- s/Int   ;;匹配比赛局数
   ;                    common-match-win-times :- s/Int ;;匹配比赛胜利次数
   ;                    common-match-update-date :- long ;;匹配比赛数据更新时间
   ;                    rank-match-times :- s/Int　      ;;排位比赛局数
   ;                    rank-match-win-times :- s/Int ;;排位比赛胜利局数
  ;                     rank-match-update-date :- long] ;;排位比赛数据更新时间
  ;)
