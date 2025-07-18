(ns bosslint.linter.yamllint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/yamllint
  (name [] "yamllint")

  (files [file-group]
    (linter/select-files file-group [:yaml]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "yamllint")
      (let [args (concat ["yamllint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (case (apply process/run args)
          0 :success
          2 :warning
          :error)))))
