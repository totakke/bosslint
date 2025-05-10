(ns bosslint.linter.shellcheck
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/shellcheck
  (name [] "ShellCheck")

  (files [file-group]
    (linter/select-files file-group [:shell]))

  (lint [{:keys [files]} _]
    (when (linter/check-command "shellcheck")
      (case (apply process/run "shellcheck" (map :absolute-path files))
        0 :success
        1 :warning
        :error))))
