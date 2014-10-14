(ns zjy.domain
  ^{:doc "domain in the zhao ji you"}
  (:require [schema.core :as s]))

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
  ^{:doc "active an account by the game id, this account may not have password. "}
  [game-base-info attitudes abilities summores]
  ())

(defn update-account
  ^{:doc "update the account, the info of member includes:
          1. roles with level
          2. positions are good at
          3. credit owned
          4. tags owned
          5. history of match by zjy
          "
    }
  [])

(s/defrecord GameBasicInfo [level :- s/Int  ;;等级
                            rank-game-level :- s/Int　　;;排位等级,例如黄铜
                            rank-game-level-num :- s/Int  ;;排位级别,例如V
                            common-match-times :- s/Int  ;;匹配比赛局数
                            common-match-win-times :- s/Int ;;匹配比赛胜利次数
                            common-match-update-date :- Long ;;匹配比赛数据更新时间
                            rank-match-times :- s/Int　;;排位比赛局数
                            rank-match-win-times :- s/Int ;;排位比赛胜利局数
                            rank-match-update-date :- Long] ;;排位比赛数据更新时间
  )


(s/defrecord PersonInfo [dirty-words :- (s/enum :hate :no-matter :accept);;是否允许脏话
                         age :- (s/enum :little :yung :adult) ;;年龄范围
                         password :- (s/Str) ;;密码
                         ]) ;;

(s/defrecord Ability [rpm :- (s/Int) ;;手速


                      ])

(s/defrecord Credit [id :- (s/Int) ;;用户id
                     init :- (s/Int)     ;;原始积分
                   　current :- (s/Int) ;;现有的积分
                     ])
