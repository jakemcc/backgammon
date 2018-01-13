(ns bg.main
  (:require [cljsjs.firebase]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [bg.events :as events]
            [bg.firebase :as firebase]
            [bg.views :as views]
            [bg.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (firebase/init)
  (mount-root))

;; TODO
;; - Mention anonymous auth 
