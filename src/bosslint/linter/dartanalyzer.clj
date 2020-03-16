(ns bosslint.linter.dartanalyzer
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/dartanalyzer
  (name [] "dartanalyzer")

  (files [file-group]
    (linter/select-files file-group [:dart]))

  (lint [files conf]
    (when (linter/check-command "dartanalyzer")
      (let [args (concat ["dartanalyzer"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
