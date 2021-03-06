(ns bosslint.linter.yamllint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/yamllint
  (name [] "yamllint")

  (files [file-group]
    (linter/select-files file-group [:yaml]))

  (lint [files conf]
    (when (linter/check-command "yamllint")
      (let [args (concat ["yamllint"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
