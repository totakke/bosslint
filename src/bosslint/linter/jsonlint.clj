(ns bosslint.linter.jsonlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/jsonlint
  (name [] "jsonlint")

  (files [file-group]
    (linter/select-files file-group [:json]))

  (lint [files conf]
    (when (linter/check-command "jsonlint")
      (doseq [file files]
        (let [args (concat ["jsonlint" (:absolute-path file)]
                           (:command-options conf))
              ret (apply shell/sh args)]
          (println (str (:git-path file) ":"))
          (when-not (string/blank? (:out ret))
            (println (string/trim-newline (:out ret))))
          (when-not (string/blank? (:err ret))
            (println (string/trim-newline (:err ret)))))))))
