(ns bosslint.linter.yamllint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/yamllint
  (name [] "yamllint")

  (files [file-group]
    (linter/select-files file-group [:yaml]))

  (lint [files conf]
    (when (linter/check-command "yamllint")
      (let [args (concat ["yamllint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (apply process/run args)))))
