(ns ^{:doc "command handlers"}
  raiseup.ontime.command-handler
  (:require [raiseup.ontime.domain :refer :all]
            [cqrs.protocol :refer [CommandHandler]])
  (:import (raiseup.ontime.commands DeleteTaskSlot StartTaskSlot CreateTaskSlot)))



(extend-type DeleteTaskSlot
  CommandHandler
  (handle-command [cmd]
    (delete-task cmd)))
(extend-type StartTaskSlot
  CommandHandler
  (handle-command [cmd]
    (start-task cmd)))
(extend-type CreateTaskSlot
  CommandHandler
  (handle-command [cmd]
    (create-task cmd)))
