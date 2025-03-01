(ns bosslint.linter.ansible-lint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/ansible-lint
  (name [] "ansible-lint")

  (files [file-group]
    (linter/select-files file-group [:yaml]))

  (lint [files conf]
    (when (linter/check-command "ansible-lint")
      (let [args (concat ["ansible-lint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
