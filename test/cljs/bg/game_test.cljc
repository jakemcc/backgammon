(ns bg.game-test
  (:require [clojure.test :refer [deftest is testing]]
            [bg.game :as game]))

(def empty-board (vec (repeat 24 [])))

(deftest test-moving-piece
  (is (= {:points [[:black] []]
          :bar []}
         (game/move-piece {:points [[] [:black]]
                           :bar []}
                          :black
                          1
                          0)))

  (is (= {:points [[:black] []]
          :bar [:red]}
         (game/move-piece {:points [[:red] [:black]]
                           :bar []}
                          :black
                          1
                          0))))


(deftest test-valid-moves
  (testing "Can move to empty locations"
    (is (= #{2 3}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:black])}
                           :black
                           0
                           [2 3]))))

  (testing "cannot move off board when you cannot bear off"
    (is (= #{23}
           (game/valid-moves {:points (assoc empty-board
                                           5 [:black]
                                           20 [:black])
                            :bar []}
                           :black
                           20
                           [3 4]))))

  (testing "cannot move to point with two peices of opposing color"
    (is (= #{}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:black]
                                           1 [:red :red])}
                           :black
                           0
                           [1]))))

  (testing "can move to location with one piece of opposing color"
    (is (= #{1}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:black]
                                           1 [:red])}
                           :black
                           0
                           [1]))))

  (testing "can move to location with own color"
    (is (= #{1}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:black]
                                           1 [:black])}
                           :black
                           0
                           [1]))))

  (testing "black cannot move off of bar"
    (is (= #{}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:red :red]
                                           1 [:red :red]
                                           2 [:red :red]
                                           3 [:red :red]
                                           4 [:red :red]
                                           5 [:red :red])
                            :bar [:black]}
                           :black
                           :bar
                           [2 4]))))

  (testing "black can move off bar"
    (is (= #{0 1 2 3 4 5}
           (game/valid-moves {:points empty-board
                            :bar [:black]}
                           :black
                           :bar
                           [1 2 3 4 5 6]))))

  (testing "black can move to one spot off bar"
    (is (= #{1}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:red :red])
                            :bar [:black]}
                           :black
                           :bar
                           [1 2]))))

  (testing "red can move off of bar"
    (is (= #{23 22 21 20 19 18}
           (game/valid-moves {:points empty-board
                            :bar [:red]}
                           :red
                           :bar
                           [1 2 3 4 5 6]))))

  (testing "Some scenario that came from clicking around that wasn't covered above.."
    (is (= #{5}
           (game/valid-moves {:points [[:black :black] ; 0
                                     [] [] [] [] ; 1, 2, 3, 4
                                     [:red :red :red :red :red] ; 5
                                     [:red] ; 6
                                     [:red :red :red] ; 7
                                     [:red] ; 8
                                     [:red] ; 9
                                     [:red :red] ; 10
                                     [:black :black :black :black :black]
                                     [:red :red] [] [] []
                                     [:black :black :black] []
                                     [:black :black :black :black :black] [] [] [] [] []]
                            :bar []}
                           :red
                           7
                           [2]))))


  (testing "Bearing off is a valid move for red"
    (is (= #{:off}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:red])
                            :bar []}
                           :red
                           0
                           [6]))))

  (testing "Bearing off is a valid move for black"
    (is (= #{:off}
           (game/valid-moves {:points (assoc empty-board
                                           23 [:black])
                            :bar []}
                           :black
                           23
                           [6]))))

  (testing "Red cannot bear off lower point because of exact die match with higher point"
    (is (= #{}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:red]
                                           5 [:red])
                            :bar []}
                           :red
                           0
                           [6]))))

  (testing "Red cannot bear off lower point because higher peice can use entire die"
    (is (= #{}
           (game/valid-moves {:points (assoc empty-board
                                           0 [:red]
                                           5 [:red])
                            :bar []}
                           :red
                           0
                           [3]))))

  (testing "Red cannot bear off lower point because non-exact higher point needs to use high die"
    (is (= #{1}
           (game/valid-moves {:points (assoc empty-board
                                           3 [:red]
                                           4 [:red])
                            :bar []}
                           :red
                           3
                           [2 6]))))

  (testing "Player can move higher point even though lower point could bear off with exact match"
    (testing "when higher point is highest point"
      (is (= #{0}
             (game/valid-moves {:points (assoc empty-board
                                             4 [:red]
                                             5 [:red])
                              :bar []}
                             :red
                             5
                             [5]))))

    (testing "when there is a higher point"
      (is (= #{0}
             (game/valid-moves {:points (assoc empty-board
                                             3 [:red]
                                             4 [:red]
                                             5 [:red])
                              :bar []}
                             :red
                             4
                             [4])))))

  (testing "Red cannot bear off lower point but can advance it"
    (is (= #{1}
           (game/valid-moves {:points (assoc empty-board
                                           2 [:red]
                                           5 [:red])
                            :bar []}
                           :red
                           2
                           [1 6]))))

  (testing "Red can bear off lower piece because of exact match"
    (is (= #{:off}
           (game/valid-moves {:points (assoc empty-board
                                           2 [:red]
                                           5 [:red])
                            :bar []}
                           :red
                           2
                           [3 6]))))

  (testing "Black cannot bear off peice lower point because of exact die match with higher point"
    (is (= #{}
           (game/valid-moves {:points (assoc empty-board
                                           23 [:black]
                                           18 [:black])
                            :bar []}
                           :black
                           23
                           [6]))))

  (testing "Black cannot bear off peice lower point but can advance it"
    (is (= #{22}
           (game/valid-moves {:points (assoc empty-board
                                           21 [:black]
                                           18 [:black])
                            :bar []}
                           :black
                           21
                           [1 6]))))

  (testing "Black cannot bear off peice in point lower point because non-exact higher point needs to use high die"
    (is (= #{22}
           (game/valid-moves {:points (assoc empty-board
                                           20 [:black]
                                           19 [:black])
                            :bar []}
                           :black
                           20
                           [2 6]))))

  (testing "Black can bear off lower piece because of exact match"
    (is (= #{:off}
           (game/valid-moves {:points (assoc empty-board
                                           21 [:black]
                                           18 [:black])
                            :bar []}
                           :black
                           21
                           [3 6]))))

  ;; Example board. Left part of line is where pieces can be moved off.
  ;; red:   (0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23)
  ;; black: (23 22 21 20 19 18 17 16 15 14 13 12 11 10 9  8  7  6  5
  ;; 4  3  2  1  0)
  )


(deftest test-updating-dice
  (testing "Simple move"
    (is (= [3] (game/update-dice [2 3] 1 3))))
  (testing "move with quads"
    (is (= [3 3 3] (game/update-dice [3 3 3 3] 22 19))))

  (testing "Move from bar"
    (is (= [  2 3 4 5 6] (game/update-dice [1 2 3 4 5 6] :bar 0)))
    (is (= [1   3 4 5 6] (game/update-dice [1 2 3 4 5 6] :bar 1)))
    (is (= [1 2   4 5 6] (game/update-dice [1 2 3 4 5 6] :bar 2)))
    (is (= [1 2 3   5 6] (game/update-dice [1 2 3 4 5 6] :bar 3)))
    (is (= [1 2 3 4   6] (game/update-dice [1 2 3 4 5 6] :bar 4)))
    (is (= [1 2 3 4 5  ] (game/update-dice [1 2 3 4 5 6] :bar 5))))

  (testing "Move from bar"
    (is (= [  2 3 4 5 6] (game/update-dice [1 2 3 4 5 6] :bar 23)))
    (is (= [1   3 4 5 6] (game/update-dice [1 2 3 4 5 6] :bar 22)))
    (is (= [1 2   4 5 6] (game/update-dice [1 2 3 4 5 6] :bar 21)))
    (is (= [1 2 3   5 6] (game/update-dice [1 2 3 4 5 6] :bar 20)))
    (is (= [1 2 3 4   6] (game/update-dice [1 2 3 4 5 6] :bar 19)))
    (is (= [1 2 3 4 5  ] (game/update-dice [1 2 3 4 5 6] :bar 18))))

  (testing "Moving off board"
    (testing "Use exact point match"
      (is (= [  2 3 4 5 6] (game/update-dice [1 2 3 4 5 6] 23 :off)))
      (is (= [1   3 4 5 6] (game/update-dice [1 2 3 4 5 6] 22 :off)))
      (is (= [1 2   4 5 6] (game/update-dice [1 2 3 4 5 6] 21 :off)))
      (is (= [1 2 3   5 6] (game/update-dice [1 2 3 4 5 6] 20 :off)))
      (is (= [1 2 3 4   6] (game/update-dice [1 2 3 4 5 6] 19 :off)))
      (is (= [1 2 3 4 5  ] (game/update-dice [1 2 3 4 5 6] 18 :off)))

      (is (= [  2 3 4 5 6] (game/update-dice [1 2 3 4 5 6] 0 :off)))
      (is (= [1   3 4 5 6] (game/update-dice [1 2 3 4 5 6] 1 :off)))
      (is (= [1 2   4 5 6] (game/update-dice [1 2 3 4 5 6] 2 :off)))
      (is (= [1 2 3   5 6] (game/update-dice [1 2 3 4 5 6] 3 :off)))
      (is (= [1 2 3 4   6] (game/update-dice [1 2 3 4 5 6] 4 :off)))
      (is (= [1 2 3 4 5  ] (game/update-dice [1 2 3 4 5 6] 5 :off))))

    (testing "Use die with higher value than point"
      (is (= [2] (game/update-dice [2 6] 4 :off)))
      (is (= [2] (game/update-dice [2 6] 19 :off))))))


;; todo: http://www.bkgm.com/rules/rul-faq.html double check the "bear off from lower" answer
(deftest test-can-bear-off?
  (testing "Red bearing off"
    (testing "when all pieces in first 6 slots"
      (is (= true (game/can-bear-off? {:points (assoc empty-board
                                                    0 [:red :red]
                                                    5 [:red :red])
                                     :bar []}
                                    :red))))

    (testing "cannot bear off because of piece outside of home"
      (is (= false (game/can-bear-off? {:points (assoc empty-board
                                                     0 [:red :red]
                                                     6 [:red])
                                      :bar []}
                                     :red))))

    (testing "cannot bear off because a piece is on bar"
      (is (= false (game/can-bear-off? {:points (assoc empty-board
                                                     0 [:red :red])
                                      :bar [:red]}
                                     :red)))))

  (testing "Black bearing off"
    (testing "when all pieces in home"
      (is (= true (game/can-bear-off? {:points (assoc empty-board
                                                    23 [:black :black]
                                                    18 [:black :black])
                                     :bar []}
                                    :black))))

    (testing "cannot bear off because of piece outside of home"
      (is (= false (game/can-bear-off? {:points (assoc empty-board
                                                     23 [:black :black]
                                                     17 [:black])
                                      :bar []}
                                     :black))))

    (testing "cannot bear off because a piece is on bar"
      (is (= false (game/can-bear-off? {:points (assoc empty-board
                                                     23 [:black :black])
                                      :bar [:black]}
                                     :black))))))

(deftest test-can-roll-dice?
  (is (= false (game/can-roll-dice? :red :black true)))
  (is (= false (game/can-roll-dice? :red :black false)))
  (is (= false (game/can-roll-dice? :red :red true)))
  (is (= true (game/can-roll-dice? :red :red false))))

(deftest test-can-end-turn?
  (testing "Can end turn when no moves left"
    ;; not true, you have to have at least rolled once.. fix later
    (is (= true (game/can-end-turn? {:board {:points empty-board}
                                   :dice []}
                                  :red))))

  (testing "Can end turn because cannot move off bar"
    (is (= true (game/can-end-turn? {:board {:points (assoc empty-board
                                                          0 [:red :red]
                                                          1 [:red :red]
                                                          2 [:red :red]
                                                          3 [:red :red]
                                                          4 [:red :red]
                                                          5 [:red :red]
                                                          15 [:black :black])
                                           :bar [:black]}
                                   :dice [3 4]}
                                  :black))))

  (testing "Cannot end turn because valid moves off bar"
    (is (= false (game/can-end-turn? {:board {:points (assoc empty-board
                                                           0 [:red :red]
                                                           1 [:red :red]
                                                           4 [:red :red]
                                                           5 [:red :red]
                                                           15 [:black :black])
                                            :bar [:black]}
                                    :dice [3 4]}
                                   :black))))

  (testing "Cannot end turn because valid exists"
    (is (= false (game/can-end-turn? {:board {:points (assoc empty-board
                                                           0 [:red :red]
                                                           1 [:red :red]
                                                           4 [:red :red]
                                                           5 [:red :red]
                                                           15 [:black :black])
                                            :bar [:black]}
                                    :dice [3 4]}
                                   :red))))

  (testing "Cannot move any piece on a point so turn can be ended"
    (is (= true (game/can-end-turn? {:board {:points (assoc empty-board
                                                          6 [:black :black]
                                                          7 [:red :red])
                                           :bar []}
                                   :dice [1 1 1 1]}
                                  :red))))

  (testing "Cannot move piece because you can't bear off non-exact lower points when higher points blocked"
    (is (= true (game/can-end-turn? {:board {:points (assoc empty-board
                                                            0 [:black :black]
                                                            1 [:red]
                                                            5 [:red :red])
                                           :bar []}
                                     :dice [5 5 5]}
                                    :red))))

  (testing "Can't end turn because exact point match for bearing off"
    (is (= false (game/can-end-turn? {:board {:points (assoc empty-board
                                                            0 [:black :black]
                                                            1 [:red]
                                                            2 []
                                                            3 [:red]
                                                            4 [:red]
                                                            5 [:red :red])
                                             :bar []}
                                     :dice [5 5 5]}
                                     :red))))

  (testing "something"
    (is (= true (game/can-end-turn? {:board {:points [[:black :black :black]
                                                      [:red :red :red :red :red :red :red :red :red]
                                                      []
                                                      [:red]
                                                      []
                                                      [:red :red :red :red :red]
                                                      [] [] [] [] [] [] [] [] [] [] [] [] []
                                                      [:black :black] [] []
                                                      [:black :black :black]
                                                      [:black :black :black :black :black :black :black]]
                                             :bar []}
                                     :dice [5 5 5]
                                     :selected-point nil}
                                    :red)))))


(deftest test-rolling-dice
  (testing "Rolling dice in initial board state NEVER results in doubles"
    (is (every? #{2} (distinct (map count (repeatedly 1000 #(game/roll-dice game/initial-board))))))))
