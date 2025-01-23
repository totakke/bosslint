(ns bosslint.linter.markdownlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/markdownlint
  (name [] "markdownlint-cli")

  (files [file-group]
    (linter/select-files file-group [:markdown]))

  (lint [files conf]
    (when (linter/check-command "markdownlint")
      (let [args (concat ["markdownlint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (case (apply process/run args)
          0 :success
          1 :warning
          :error)))))
