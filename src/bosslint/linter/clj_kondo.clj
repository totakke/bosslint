(ns bosslint.linter.clj-kondo
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]))

(deflinter :linter/clj-kondo
  (name [] "clj-kondo")

  (files [diff-files]
    (linter/select-files diff-files [:clj :cljc :cljs]))

  (lint [files _]
    (when (linter/check-command "clj-kondo")
      (let [ret (apply shell/sh "clj-kondo" "--lint"
                       (map :absolute-path files))]
        (println (:out ret))))))
