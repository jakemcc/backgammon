(ns bg.views
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [bg.subs :as subs]
            [bg.game :as game]))

(defn listen [x & xs]
  @(rf/subscribe (vec (cons x xs))))

(defn random-four-characters []
  (->> (repeatedly #(rand-nth ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"]))
       (take 4)
       (str/join)
       (str/upper-case)))

(defn select-indices [indices coll]
  (map #(nth (vec coll) %) indices))

(defn range-incl [start end step]
  (let [adjust (if (pos? step) inc dec)]
    (range start (adjust end) step)))

(defn piece-bar-view
  [color bar-index]
  (let [my-player (listen :me)
        current-player (listen :current-player)
        can-select? (= color (:player my-player) (:player current-player))]
    [:div (cond-> {:class (str "piece " (name color))}
            (and (game/has-piece-on-bar? (listen :board) color)
                 can-select?)
            (assoc :on-click (fn []
                               (rf/dispatch [:select-point :bar]))))]))

(defn piece-view [color point-index]
  (let [can-select? (and (not (game/has-piece-on-bar? (listen :board) color))
                         (= color (:player (listen :me)) (:player (listen :current-player))))]
    [:div (cond-> {:class (str "piece " (name color))}
            can-select?
            (assoc :on-click (fn []
                               (rf/dispatch [:select-point point-index]))))]))

(defn point-view [idx point move-candidate?]
  (let [classes ["point"
                 (cond
                   (= :off idx) "off"
                   (even? idx) "light"
                   :else "dark")
                 (if move-candidate? "highlight" "")]]
    [:div (cond-> {:class (str/join " " classes)}
            move-candidate?
            (assoc :on-click (fn [] (rf/dispatch [:move-piece-on idx]))))
     (for [[p-idx piece] (map vector (range) point)]
       ^{:key (str idx "-piece-" p-idx)}
       [piece-view piece idx])]))

(defn controls []
  (let [game-stage (listen :game-stage)
        my-player (listen :me)
        current-player (listen :current-player)
        game (listen :game)]
    [:div.controls
     [:p "Game Code: " [:b (listen :game-id)]]
     [:ul {:class "list-unstyled list-inline"}
      [:li [:button {:class ["btn" "btn-sm" "btn-default"]
                     :on-click #(rf/dispatch [:start-game])}
            "Start game!"]]
      #_      [:li [:button (cond-> {:class ["btn" "btn-default"]
                                     :on-click randomize-turn}
                              (= :starting game-stage)
                              (assoc :disabled "disabled"))
                    "Randomize starting player"]]
      [:li [:button {:class ["btn" "btn-sm" "btn-default"]
                     :on-click  #(rf/dispatch [:become-player :red])}
            [:div "Become " [:div {:class "inline piece red"}]]]]
      [:li [:button {:class ["btn" "btn-sm" "btn-default"]
                     :on-click  #(rf/dispatch [:become-player :black])}
            [:div "Become " [:div {:class "inline piece black"}]]]]]
     (when (= :playing game-stage)
       [:ul {:class "list-unstyled list-inline"}
        [:li [:button (cond-> {:class ["btn" "btn-sm" "btn-default"]
                               :on-click #(rf/dispatch [:roll-dice])}
                        (or (= :randomizing-start game-stage)
                            (not (game/can-roll-dice? (:player my-player)
                                                      (:player current-player)
                                                      (listen :has-rolled-dice?))))
                        (assoc :disabled "disabled")

                        (and (not= :randomizing-start game-stage)
                             (listen :my-turn?)
                             (not (listen :has-rolled-dice?)))
                        (update :class conj "btn-success"))
              "Roll dice"]]

        [:li [:button
              (let [can-end-turn? (listen :can-end-turn?)]
                (cond-> {:class
                         ["btn" "btn-sm" (if can-end-turn?
                                           "btn-success"
                                           "btn-default")]
                         :on-click #(rf/dispatch [:end-turn])}
                  (not can-end-turn?)
                  (assoc :disabled "disabled")))
              "End turn"]]

        [:li [:button (cond-> {:on-click #(rf/dispatch [:undo-moves])
                               :class ["btn" "btn-sm" (if (and (listen :has-rolled-dice?)
                                                               (listen :my-turn?)
                                                               (empty? (:dice game)))
                                                        "btn-danger"
                                                        "btn-default")]}
                        (or (nil? (:beginning-of-turn-state current-player))
                            (not (listen :my-turn?)))
                        (assoc :disabled "disabled"))
              "Undo moves"]]
        [:li [:span "Player's turn: " [:div {:class (str "inline piece " (name (:player current-player)))}]]]])]))

(defn winner-banner []
  (when-let [winner (listen :winner)]
    [:h3 "Player " [:div {:class ["inline" "piece" (name winner)]}] " won!!"]))

(defn board []
  (let [my-player (listen :me)
        game (listen :game)]
    (when-let [points (seq (get-in game [:board :points]))]
      (let [potential-moves (listen :valid-moves)]
        [:div.board
         (let [points-with-index (map vector (range) points)]
           [:div [:div {:class "side left"}
                  [:div {:class "quarter top"}
                   (for [[idx point] (select-indices (range-incl 11 6 -1) points-with-index)
                         :let [move-candidate? (contains? potential-moves idx)]]
                     ^{:key (str idx "-point")}
                     [point-view idx point move-candidate?])]
                  [:div {:class "quarter bottom"}
                   (for [[idx point] (select-indices (range-incl 12 17 1) points-with-index)
                         :let [move-candidate? (contains? potential-moves idx)]]
                     ^{:key (str idx "-point")}
                     [point-view idx point move-candidate?])]]

            [:div#bar
             (map-indexed
              (fn [idx piece]
                ^{:key (str "bar-" idx)}
                [piece-bar-view piece idx])
              (get-in game [:board :bar]))
             (when-let [dice (seq (:dice game))]
               [:div
                [:div {:class (str "die die-" (first dice)) :id "die1"}]
                [:div {:class (str "die die-" (second dice)) :id "die2"}]])]

            [:div {:class "side right"}
             [:div {:class "quarter top"}
              (for [[idx point] (select-indices (range-incl 5 0 -1) points-with-index)
                    :let [move-candidate? (contains? potential-moves idx)]]
                ^{:key (str idx "-point")}
                [point-view idx point move-candidate?])
              [point-view :off
               (:red (:off (:board game)))
               (and (= :red (:player my-player))
                    (contains? potential-moves :off))]]
             [:div {:class "quarter bottom"}
              (for [[idx point] (select-indices (range-incl 18 23 1) points-with-index)
                    :let [move-candidate? (contains? potential-moves idx)]]
                ^{:key (str idx "-point")}
                [point-view idx point move-candidate?])
              [point-view :off
               (:black (:off (:board game)))
               (and (= :black (:player my-player))
                    (contains? potential-moves :off))]]]])]))))

(defn on-going-game []
  [:div
   [controls]
   [winner-banner]
   [board]])

(defn start-game-view []
  [:div {:style {:padding-left "10px"
                 :padding-top "10px"}}
   [:div {:class "row"}
    [:button {:class ["btn" "btn-default"]
              :on-click #(rf/dispatch [:join-game (random-four-characters)])}
     "New game"]]
   [:div.row "or"]
   [:form {:class "form-inline form-group row"
           :on-submit (fn [e]
                        (.preventDefault e)
                        (rf/dispatch [:join-game (str/upper-case (.. e -target -elements -game -value))]))}
    [:input {:class "form-control"
             :style {:text-transform :uppercase}
             :type "text"
             :name "game"
             :placeholder "Enter Game Code"
             :required true}]
    [:button {:class ["btn" "btn-default"] :type "submit"} "Join Game"]]])

(defn main-panel []
  [:div
   [:div {:class "container"}
    (if @(rf/subscribe [:game-id])
      [on-going-game]
      [start-game-view])]])
