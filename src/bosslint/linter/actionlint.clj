(ns bosslint.linter.actionlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/actionlint
  (name [] "actionlint")

  (files [file-group]
    (linter/select-files file-group [:workflow]))

  (lint [files _]
    (when (linter/check-command "actionlint")
      (case (apply process/run "actionlint" (map :absolute-path files))
        0 :success
        1 :warning
        :error))))
