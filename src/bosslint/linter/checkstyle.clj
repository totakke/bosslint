(ns bosslint.linter.checkstyle
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/checkstyle
  (name [] "checkstyle")

  (files [file-group]
    (linter/select-files file-group [:java]))

  (lint [files conf]
    (when (linter/check-command "checkstyle")
      (let [args (concat ["checkstyle"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
