(ns bosslint.linter.flake8
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/flake8
  (name [] "Flake8")

  (files [file-group]
    (linter/select-files file-group [:python]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "flake8")
      (let [args (concat ["flake8"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
