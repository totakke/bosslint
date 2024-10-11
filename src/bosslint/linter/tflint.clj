(ns bosslint.linter.tflint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/tflint
  (name [] "tflint")

  (files [file-group]
    (linter/select-files file-group [:terraform]))

  (lint [files conf]
    (when (linter/check-command "tflint")
      (let [args (concat ["tflint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (apply process/run args)))))
