(ns bosslint.linter.stylelint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/stylelint
  (name [] "stylelint")

  (files [file-group]
    (linter/select-files file-group [:css :sass]))

  (lint [files conf]
    (when (linter/check-command "stylelint")
      (let [args (concat ["stylelint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (apply process/run args)))))
