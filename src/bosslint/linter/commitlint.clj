(ns bosslint.linter.commitlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/commitlint
  (name [] "commitlint")

  (files [file-group]
    [:dummy])

  (lint [{:keys [ref1 ref2]} _]
    (when (and ref1 (linter/check-command "commitlint"))
      (let [args (concat ["commitlint"
                          "--from" ref1]
                         (when ref2
                           ["--to" ref2]))]
        (case (apply process/run args)
          0 :success
          2 :warning
          :error)))))
