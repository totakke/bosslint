(ns bosslint.linter.swiftlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/swiftlint
  (name [] "Swiftlint")

  (files [file-group]
    (linter/select-files file-group [:swift]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "swiftlint")
      (let [args (concat ["swiftlint" "lint"]
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
