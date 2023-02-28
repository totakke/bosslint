(ns bosslint.linter.sql-lint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/sql-lint
  (name [] "sql-lint")

  (files [file-group]
    (linter/select-files file-group [:sql]))

  (lint [files conf]
    (when (linter/check-command "sql-lint")
      (doseq [file files]
        (let [args (concat ["sql-lint"]
                           (:command-options conf)
                           [(:absolute-path file)])
              ret (apply shell/sh args)]
          (when-not (string/blank? (:out ret))
            (println (string/trim-newline (:out ret))))
          (when-not (string/blank? (:err ret))
            (println (string/trim-newline (:err ret)))))))))
