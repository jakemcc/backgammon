(ns bg.events
  (:require [re-frame.core :as rf]
            [bg.game :as game]
            [bg.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   {}))

(rf/reg-event-db
 :sync
 (fn [db [_ data]]
   (assoc db :external data)))

(rf/reg-event-fx
 :join-game
 (fn [{:keys [db]} [_ game-id]]
   {:db (assoc db :game-id game-id)
    :firebase/subscribe {:game-id game-id
                         :default db/default-db}}))

(rf/reg-event-fx
 :start-game
 (fn [coeffects _]
   (let [external (get-in coeffects [:db :external])]
     {:firebase/set {:game-id (:game-id (:db coeffects))
                     :data (-> external
                               (assoc :current-player {:player :red})
                               (assoc :game-stage :playing)
                               (assoc :game {:board game/initial-board}))}})))


;; Change :player key to :color

(rf/reg-event-db
 :become-player
 (fn [db [_ color]]
   (assoc db :me {:player color})))

(rf/reg-event-fx
 :select-point
 (fn [{:keys [db]} [_ choice]]
   {:firebase/set {:game-id (:game-id db)
                   :data (assoc-in (:external db) [:game :selected-point] choice)}}))

(rf/reg-event-fx
 :roll-dice
 (fn [coeffects _]
   (let [external (get-in coeffects [:db :external])
         game-id (get-in coeffects [:db :game-id])
         dice (game/roll-dice (get-in external [:game :board]))
         updated-game (assoc (:game external) :dice dice)]
     {:firebase/set {:game-id game-id
                     :data (-> external
                               (assoc :game updated-game)
                               (assoc-in [:current-player :has-rolled-dice?] true)
                               (assoc-in [:current-player :has-rolled-dice?] true)
                               (assoc-in [:current-player :beginning-of-turn-state] updated-game))}})))

(rf/reg-event-fx
 :move-piece-on
 (fn [{:keys [db]} [_ point]]
   (let [{:keys [board dice selected-point]} (get-in db [:external :game])
         current-player (get-in db [:external :current-player])]
     {:firebase/set {:game-id (:game-id db)
                     :data (-> (:external db)
                               (assoc-in [:game :board] (game/move-piece board (:player current-player) selected-point point))
                               (assoc-in [:game :selected-point] nil)
                               (assoc-in [:game :dice] 
                                         (game/update-dice dice selected-point point)))}})))

(rf/reg-event-fx
 :undo-moves
 (fn [{:keys [db]} _]
   (let [external (:external db)]
     {:firebase/set {:game-id (:game-id db)
                     :data (assoc external :game (get-in external [:current-player :beginning-of-turn-state]))}})))

(rf/reg-event-fx
 :end-turn
 (fn [{:keys [db]} _]
   (let [external (:external db)]
     {:firebase/set {:game-id (:game-id db)
                     :data (-> external
                               (assoc-in [:game :selected-point] nil)
                               (assoc-in [:game :dice] [])
                               (update :current-player (fn [p]
                                                         {:player (game/opposing-piece (:player p))
                                                          :has-rolled-dice? false
                                                          :beginning-of-turn-state nil})))}})))


