(ns bosslint.linter.hadolint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]))

(deflinter :linter/hadolint
  (name [] "hadolint")

  (files [diff-files]
    (linter/select-files diff-files [:docker]))

  (lint [files _]
    (when (linter/check-command "hadolint")
      (let [ret (apply shell/sh "hadolint" (map :absolute-path files))]
        (println (:out ret))))))
