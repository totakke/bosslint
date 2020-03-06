(ns bosslint.linter.clj-kondo
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/clj-kondo
  (name [] "clj-kondo")

  (files [file-group]
    (linter/select-files file-group [:clj :cljc :cljs]))

  (lint [files _]
    (when (linter/check-command "clj-kondo")
      (let [ret (apply shell/sh "clj-kondo" "--lint"
                       (map :absolute-path files))]
        (println (string/trim-newline (:out ret)))))))
