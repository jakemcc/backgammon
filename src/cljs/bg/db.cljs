(ns bg.db
  (:require [bg.game :as game]))

(def default-db
  {:game {:board game/initial-board}
   :stage :starting
   :current-player {:player :red}})
