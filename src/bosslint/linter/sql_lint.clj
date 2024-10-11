(ns bosslint.linter.sql-lint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/sql-lint
  (name [] "sql-lint")

  (files [file-group]
    (linter/select-files file-group [:sql]))

  (lint [files conf]
    (when (linter/check-command "sql-lint")
      (doseq [file files]
        (let [args (concat ["sql-lint"]
                           (:command-options conf)
                           [(:absolute-path file)])]
          (apply process/run args))))))
