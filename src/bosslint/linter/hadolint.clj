(ns bosslint.linter.hadolint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/hadolint
  (name [] "hadolint")

  (files [file-group]
    (linter/select-files file-group [:docker]))

  (lint [files _]
    (when (linter/check-command "hadolint")
      (if (zero? (apply process/run "hadolint" (map :absolute-path files)))
        :success
        :error))))
