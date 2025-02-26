(ns bosslint.linter.tflint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/tflint
  (name [] "tflint")

  (files [file-group]
    (linter/select-files file-group [:terraform]))

  (lint [files conf]
    (when (linter/check-command "tflint")
      (let [args (concat ["tflint" "--recursive"]
                         (:command-options conf)
                         (mapcat #(vector "--filter" (:git-path %)) files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
