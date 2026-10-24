(ns bosslint.linter.ruff
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/ruff
  (name [] "Ruff")

  (files [file-group _]
    (linter/select-files file-group [:python]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "ruff")
      (let [args (concat ["ruff" "check"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
