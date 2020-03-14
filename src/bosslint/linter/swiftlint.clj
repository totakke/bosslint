(ns bosslint.linter.swiftlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/swiftlint
  (name [] "swiftlint")

  (files [file-group]
    (linter/select-files file-group [:swift]))

  (lint [files conf]
    (when (linter/check-command "swiftlint")
      (let [args (concat ["swiftlint" "lint"]
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
