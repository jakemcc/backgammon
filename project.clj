(defproject bg "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"] ;; 1.8.0
                 [org.clojure/clojurescript "1.9.946"] ;; 1.9.908
                 [reagent "0.8.0-alpha2"] ;; 0.7.0
                 [re-frame "0.10.3-alpha2"]
                 [cljsjs/firebase "4.8.1-0"]]

  :plugins [[lein-cljsbuild "1.1.7"] ;"1.1.5"
            ]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj" "test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   ;; [day8.re-frame/trace "0.1.14"]
                   [re-frisk "0.5.3"]]

    :plugins      [[lein-figwheel "0.5.14"]  ; 0.5.13
                   [lein-doo "0.1.8"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "bg.main/mount-root"}
     :compiler     {:main                 bg.main
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           ;; day8.re-frame.trace.preload
                                           re-frisk.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            bg.main
                    :output-to       "resources/public/js/compiled/app.js"
                    ;; hmm, firebase failing to work with :advanced build, this doens't seem to look inside a jar either,
                    ;; :foreign-libs [{:file "cljsjs/development/firebase.inc.js",
                    ;;                 :provides ["cljsjs.firebase"]}]
                    :optimizations :whitespace
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          bg.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]})
