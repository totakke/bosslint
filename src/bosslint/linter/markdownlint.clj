(ns bosslint.linter.markdownlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/markdownlint
  (name [] "markdownlint")

  (files [file-group]
    (linter/select-files file-group [:markdown]))

  (lint [files conf]
    (when (linter/check-command "markdownlint")
      (let [args (concat ["markdownlint"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (when-not (string/blank? (:out ret))
          (println (string/trim-newline (:out ret))))
        (when-not (string/blank? (:err ret))
          (println (string/trim-newline (:err ret))))))))
