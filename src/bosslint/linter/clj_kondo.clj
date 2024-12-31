(ns bosslint.linter.clj-kondo
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/clj-kondo
  (name [] "clj-kondo")

  (files [file-group]
    (linter/select-files file-group [:clj :cljc :cljs]))

  (lint [files _]
    (when (linter/check-command "clj-kondo")
      (case (apply process/run "clj-kondo" "--lint" (map :absolute-path files))
        0 :success
        2 :warning
        :error))))
