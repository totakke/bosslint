(ns bosslint.linter.cljstyle
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/cljstyle
  (name [] "cljstyle")

  (files [file-group]
    (linter/select-files file-group [:clj :cljc :cljs]))

  (lint [{:keys [files]} _]
    (when (linter/check-command "cljstyle")
      (case (apply process/run "cljstyle" "check" (map :absolute-path files))
        0 :success
        2 :warning
        :error))))
