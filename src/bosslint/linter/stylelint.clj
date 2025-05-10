(ns bosslint.linter.stylelint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/stylelint
  (name [] "stylelint")

  (files [file-group]
    (linter/select-files file-group [:css :sass]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "stylelint")
      (let [args (concat ["stylelint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (case (apply process/run args)
          0 :success
          2 :warning
          :error)))))
