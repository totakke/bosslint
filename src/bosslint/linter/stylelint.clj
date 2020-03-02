(ns bosslint.linter.stylelint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]))

(deflinter :linter/stylelint
  (name [] "stylelint")

  (files [diff-files]
    (linter/select-files diff-files [:css :sass]))

  (lint [files]
    (when (linter/check-command "stylelint")
      (let [ret (apply shell/sh "stylelint" (map :absolute-path files))]
        (println (:out ret))))))
