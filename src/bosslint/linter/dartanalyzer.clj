(ns bosslint.linter.dartanalyzer
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/dartanalyzer
  (name [] "dartanalyzer")

  (files [file-group]
    (linter/select-files file-group [:dart]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "dartanalyzer")
      (let [args (concat ["dartanalyzer"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (apply process/run args)))))
