(ns bosslint.linter.tflint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/tflint
  (name [] "tflint")

  (files [file-group]
    (linter/select-files file-group [:terraform]))

  (lint [files conf]
    (when (linter/check-command "tflint")
      (let [args (concat ["tflint"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
