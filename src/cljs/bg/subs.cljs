(ns bg.subs
  (:require [re-frame.core :as rf]
            [bg.game :as game]))

(rf/reg-sub
 :db
 (fn [db]
   db))

(rf/reg-sub
 :me
 (fn [db]
   (:me db)))

(rf/reg-sub
 :game-id
 (fn [db]
   (:game-id db)))

(rf/reg-sub
 :external
 (fn [db]
   (:external db)))

(rf/reg-sub
 :game
 (fn [_]
   (rf/subscribe [:external]))
 (fn [external]
   (:game external)))

(rf/reg-sub
 :board
 (fn [_]
   (rf/subscribe [:game]))
 (fn [game]
   (:board game)))

(rf/reg-sub
 :game-stage
 (fn [_]
   (rf/subscribe [:external]))
 (fn [external]
   (:game-stage external)))

(rf/reg-sub
 :selected-point
 (fn [_]
   (rf/subscribe [:game]))
 (fn [game]
   (:selected-point game)))

(rf/reg-sub
 :dice
 (fn [_]
   (rf/subscribe [:game]))
 (fn [game]
   (:dice game)))

(rf/reg-sub
 :current-player
 (fn [_]
   (rf/subscribe [:external]))
 (fn [external]
   (:current-player external)))

(rf/reg-sub
 :has-rolled-dice?
 (fn [_ _]
   (rf/subscribe [:current-player]))
 (fn [current-player _]
   (boolean (:has-rolled-dice? current-player))))

(rf/reg-sub
 :my-turn?
 :<- [:me]
 :<- [:current-player]
 (fn [[me current-player]]
   (= (:player me) (:player current-player))))

(rf/reg-sub
 :valid-moves
 (fn [_ _]
   [(rf/subscribe [:game])
    (rf/subscribe [:current-player])
    (rf/subscribe [:selected-point])])
 (fn [[game player selected-point]]
   (if selected-point
     (game/valid-moves (:board game)
                       (:player player)
                       selected-point
                       (:dice game))
     #{})))

(rf/reg-sub
 :can-end-turn?
 (fn [_]
   [(rf/subscribe [:my-turn?]) 
    (rf/subscribe [:has-rolled-dice?])
    (rf/subscribe [:current-player])
    (rf/subscribe [:game])])
 (fn [[my-turn? has-rolled-dice? current-player game]]
   (or (and my-turn? has-rolled-dice? (game/can-end-turn? game (:player current-player)))
       (and my-turn? has-rolled-dice? (empty? (:dice game))))))


(rf/reg-sub
 :winner
 (fn [_]
   (rf/subscribe [:board]))
 (fn [board]
   (cond
     (= 15 (count (get-in board [:off :black])))
     :black

     (= 15 (count (get-in board [:off :red])))
     :red)))
