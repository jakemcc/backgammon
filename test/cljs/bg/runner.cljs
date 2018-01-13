(ns bg.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [bg.game-test]))

(doo-tests 'bg.game-test)
