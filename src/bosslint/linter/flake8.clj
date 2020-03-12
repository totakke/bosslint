(ns bosslint.linter.flake8
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/flake8
  (name [] "flake8")

  (files [file-group]
    (linter/select-files file-group [:python]))

  (lint [files conf]
    (when (linter/check-command "flake8")
      (let [args (concat ["flake8"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
