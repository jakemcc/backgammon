(ns bg.firebase
  (:require [cljsjs.firebase]
            [re-frame.core :as rf]
            [clojure.string :as string]
            [cljs.reader :as reader]))

(defn init []
  (js/firebase.initializeApp
   #js {:apiKey "AIzaSyAb1IwbJHuNylWap1MFraBKVYRUFk4ShsY",
        :authDomain "bg-example-81c6b.firebaseapp.com",
        :databaseURL "https://bg-example-81c6b.firebaseio.com",
        :projectId "bg-example-81c6b",
        :storageBucket "bg-example-81c6b.appspot.com",
        :messagingSenderId "391548202910"}))

(defn db-ref [path]
  (.ref (js/firebase.database) (string/join "/" path)))

(defn save! [ref data]
  (.set ref (pr-str data)))

(defn subscribe [path]
  (.on path "value"
       (fn [snapshot]
         (when-let [d (.val snapshot)]
           (rf/dispatch [:sync (reader/read-string d)])))))

(rf/reg-fx
 :firebase/subscribe
 (fn [{:keys [game-id default]}]
   (let [ref (db-ref [game-id])]
     (.once ref "value"
            (fn received [snapshot]
              (subscribe ref)
              (if-let [data (.val snapshot)]
                (rf/dispatch [:sync (reader/read-string data)])
                (do (save! ref default)
                    (rf/dispatch [:sync default]))))))))

(rf/reg-fx
 :firebase/set
 (fn [{:keys [game-id data]}]
   (save! (db-ref [game-id]) data)))
