(ns bosslint.linter.checkstyle
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]))

(deflinter :linter/checkstyle
  (name [] "checkstyle")

  (files [diff-files]
    (linter/select-files diff-files [:java]))

  (lint [files]
    (when (linter/check-command "checkstyle")
      (let [ret (apply shell/sh "checkstyle" "-c" "google_checks.xml"
                       (map :absolute-path files))]
        (println (:out ret))))))
