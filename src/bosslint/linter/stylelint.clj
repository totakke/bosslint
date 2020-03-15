(ns bosslint.linter.stylelint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/stylelint
  (name [] "stylelint")

  (files [file-group]
    (linter/select-files file-group [:css :sass]))

  (lint [files conf]
    (when (linter/check-command "stylelint")
      (let [args (concat ["stylelint"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
