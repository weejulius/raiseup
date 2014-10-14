(ns zjy.domain
  ^{:doc "domain in the zhao ji you"})

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
  ^{:doc "active an account by the game id, this account may not have password"}
  [])

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

(defn )
