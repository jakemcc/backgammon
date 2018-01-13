(ns bg.game)

(defn dbg [s x]
 (prn s x)
  x)

(def initial-board
  {:points
   [[:black :black]
    []
    []
    []
    []
    [:red :red :red :red :red]
    []
    [:red :red :red]
    []
    []
    []
    [:black :black :black :black :black]
    [:red :red :red :red :red]
    []
    []
    []
    [:black :black :black]
    []
    [:black :black :black :black :black]
    []
    []
    []
    []
    [:red :red]]
   :bar []})

(def number-of-points 24)

(defn abs [x]
  (max x (- x)))

(defn replace-when [pred elem coll]
  (map #(if (pred %)
          elem
          %)
       coll))

(defn roll-dice [board]
  (let [die1 (inc (rand-int 6))
        die2 (inc (rand-int 6))]
    (if (= die1 die2)
      ;; can't roll doubles on first roll of game
      (if (= initial-board board)
        (recur board)
        [die1 die2 die1 die2])
      [die1 die2])))

(defn remove-once [pred coll]
  ((fn inner [coll]
     (lazy-seq
      (when-let [[x & xs] (seq coll)]
        (if (pred x)
          xs
          (cons x (inner xs))))))
   coll))

(defn hit?
  "Given a list of points, piece being moved, and location moving to
  returns true if moving to that piece results in a hit."
  [points moving-piece location]
  (= 1 (count (remove #{moving-piece}
                      (get points location)))))

;; opposing-color and replace opposing-player?
(defn opposing-piece [piece]
  (if (= :red piece)
    :black
    :red))

(defn move-piece [board color from-point to-point]
  (let [move-to-location (fn [board piece to-point]
                           (if (= :off to-point)
                             (update-in board [:off piece] conj piece)
                             (if (hit? (:points board) piece to-point)
                               (-> board
                                   (update-in [:bar] conj (opposing-piece piece))
                                   (assoc-in [:points to-point] [piece]))
                               (update-in board [:points to-point] conj piece))))]
    (if (= :bar from-point)
      (-> board
          (update-in [:bar] #(remove-once #{color} %))
          (move-to-location color to-point))
      (let [piece (first (nth (:points board) from-point))
            board-without-piece (update-in board [:points from-point] rest)]
        (move-to-location board-without-piece piece to-point)))))

(defn can-bear-off? [board player]
  (let [points (:points board)
        p (if (= :red player)
            (fn [i] (< i 6))
            (fn [i] (< (- (dec number-of-points) i) 6)))
        points-with-color (keep-indexed
                           (fn [idx point]
                             (when (some #{player} point)
                               idx))
                           points)]
    (and (not-any? #{player} (:bar board))
         (every? p points-with-color))))

(defn can-move-to-location?
  "Answers the question 'Can piece move to location?'"
  [board piece location]
  (let [points (:points board)

        empty-point? (fn [location] (empty? (get points location)))
        occupied-by-own? (fn [location]
                           (some #{piece} (get points location)))
        on-board? (fn [location]
                    (or (can-bear-off? board piece)
                        (< -1 location number-of-points)))

        valid-location? (fn [location]
                          (and (on-board? location)
                               (or (empty-point? location)
                                   (hit? points piece location)
                                   (occupied-by-own? location))))]
    (valid-location? location)))

(defn valid-from-bar-moves
  [board moving-player dice]
  (let [possible-locations (map dec dice)
        possible-locations (if (= :black moving-player)
                             possible-locations
                             (map #(- (dec number-of-points) %) possible-locations))]
    (set (filter (partial can-move-to-location?
                          board
                          moving-player)
                 possible-locations))))

(defn translate-red-oriented-point-to-black-oriented
  [point]
  (- (dec number-of-points) point))

(defn remove-dice-that-must-move-another-point
  [board moving-player point dice]
  (let [dice (map abs dice)
        points-with-player (set (keep-indexed (fn [idx point]
                                                (when (some #{moving-player} point)
                                                  idx))
                                              (if (= :black moving-player)
                                                (reverse (:points board))
                                                (:points board))))
        point (if (= :black moving-player)
                (translate-red-oriented-point-to-black-oriented point)
                point)

        exact-match-for-point (filter (fn [die] (= (dec die) point)) dice)
        lower-than-point (filter (fn [die] (< (dec die) point)) dice)
        no-exact-matches (remove (fn [die] (contains? points-with-player (dec die))) dice)

        higher-points (set (filter #(> % point) points-with-player))]
    (if (not-empty higher-points)
      (concat exact-match-for-point lower-than-point)
      dice)))

(defn valid-bearing-off-moves
  [board moving-player point dice]
  (let [dice (remove-dice-that-must-move-another-point board
                                                       moving-player
                                                       point
                                                       dice)
        dice (map #(if (= :red moving-player)
                     (* -1 %)
                     %)
                  dice)
        piece (first (get (:points board) point))
        possible-locations (map #(+ point %) dice)
        potential-moves (->> possible-locations
                             (filter (partial can-move-to-location? board piece))
                             (replace-when #(or (neg? %)
                                                (>= % number-of-points))
                                           :off)
                             (set))]
    (when (= moving-player piece)
      potential-moves)))

(defn can-only-move-off? [moves]
  (= #{:off} moves))

(defn player-on-point? [board player point-index]
  (= player (first (get (:points board)
                        point-index))))

(defn exists-exact-match? [board moving-player dice]
  (let [d (map abs dice)
        points (:points board)]
    (some #(= moving-player (first %))
          (map #(get points (dec %)) d))))

(defn valid-point-moves
  [board moving-player point dice]
  (if-not (player-on-point? board moving-player point)
    #{}
    (let [dice (map #(if (= :red moving-player)
                       (* -1 %)
                       %)
                    dice)
          possible-locations (map #(+ point %) dice)
          potential-moves (->> possible-locations
                               (filter (partial can-move-to-location? board moving-player))
                               (replace-when #(or (neg? %)
                                                  (>= % number-of-points))
                                             :off)
                               (set))]
      (if (and (can-only-move-off? potential-moves)
               (exists-exact-match? board moving-player dice))
        #{}
        potential-moves))))



(defn valid-moves [board moving-player point-or-bar dice]
  (cond
    (= :bar point-or-bar)
    (valid-from-bar-moves board moving-player dice)

    (can-bear-off? board moving-player)
    (valid-bearing-off-moves board moving-player point-or-bar dice)

    :else
    (valid-point-moves board moving-player point-or-bar dice)))

(defn has-exact-match? [dice from-location]
  (some #{(inc from-location)
          (- number-of-points from-location)}
        dice))

(defn remove-exact-match [dice from-location]
  (remove-once #{(inc from-location)
                 (- number-of-points from-location)}
               dice))

(defn has-higher-die? [dice from-location]
  (let [point (min (- number-of-points from-location) from-location)]
    (some #(> % point) dice)))

(defn remove-higher-die [dice from-location]
  (let [point (min (- number-of-points from-location) from-location)]
    (remove-once #(> % point)
                 dice)))

(defn update-dice-when-bearing-off [dice from-location]
  (cond
    (has-exact-match? dice from-location)
    (remove-exact-match dice from-location)

    (has-higher-die? dice from-location)
    (remove-higher-die dice from-location)

    :else dice))

(defn update-dice [dice from-location to-location]
  (cond
    (= :bar from-location) (remove-once #{(inc to-location)
                                          (- number-of-points to-location)}
                                        dice)

    (= :off to-location) (update-dice-when-bearing-off dice
                                                       from-location)

    :else (remove-once #{(- to-location from-location)
                         (- from-location to-location)}
                       dice)))

(defn can-roll-dice? [me current-player has-rolled?]
  (and (= me current-player)
       (not has-rolled?)))

(defn has-piece-on-bar? [board piece]
  (boolean (some #{piece} (:bar board))))

(defn can-end-turn? [game moving-player]
  (if (has-piece-on-bar? (:board game) moving-player)
    (empty? (valid-moves (:board game) moving-player :bar (:dice game)))
    (every? (fn [point] (empty? (valid-moves (:board game)
                                             moving-player
                                             point
                                             (:dice game))))
            (range number-of-points))))
