(ns bosslint.linter.jsonlint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(deflinter :linter/jsonlint
  (name [] "jsonlint")

  (files [file-group]
    (linter/select-files file-group [:json]))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "jsonlint")
      (doseq [file files]
        (let [args (concat ["jsonlint" (:absolute-path file)]
                           (:command-options conf))]
          (println (str (:git-path file) ":"))
          (apply process/run args))))))
