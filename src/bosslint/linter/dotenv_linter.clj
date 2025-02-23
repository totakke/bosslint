(ns bosslint.linter.dotenv-linter
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/dotenv-linter
  (name [] "dotenv-linter")

  (files [file-group]
    (linter/select-files file-group [:dot-env]))

  (lint [files _]
    (when (linter/check-command "dotenv-linter")
      (if (zero? (apply process/run "dotenv-linter" (map :absolute-path files)))
        :success
        :error))))
