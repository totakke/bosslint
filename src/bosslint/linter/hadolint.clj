(ns bosslint.linter.hadolint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/hadolint
  (name [] "hadolint")

  (files [file-group]
    (linter/select-files file-group [:docker]))

  (lint [files _]
    (when (linter/check-command "hadolint")
      (let [ret (apply shell/sh "hadolint" (map :absolute-path files))]
        (println (string/trim-newline (:out ret)))))))
