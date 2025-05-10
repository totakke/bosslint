(ns bosslint.linter.checkstyle
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/checkstyle
  (name [] "Checkstyle")

  (files [file-group]
    (linter/select-files file-group [:java]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "checkstyle")
      (let [args (concat ["checkstyle"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
