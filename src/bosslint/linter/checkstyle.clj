(ns bosslint.linter.checkstyle
  (:require [bosslint.config :as config]
            [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/checkstyle
  (name [] "checkstyle")

  (files [file-group]
    (linter/select-files file-group [:java]))

  (lint [files conf]
    (when (linter/check-command "checkstyle")
      (let [opt-config (config/resolve-path (:config conf))
            args (flatten
                  (cond-> ["checkstyle"]
                    opt-config (concat ["-c" opt-config])
                    true (concat (map :absolute-path files))))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
