(ns bosslint.linter.checkstyle
  (:require [bosslint.config :as config]
            [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]))

(deflinter :linter/checkstyle
  (name [] "checkstyle")

  (files [diff-files]
    (linter/select-files diff-files [:java]))

  (lint [files conf]
    (when (linter/check-command "checkstyle")
      (let [opt-config (config/resolve-path (:config conf))
            args (flatten
                  (cond-> ["checkstyle"]
                    opt-config (concat ["-c" opt-config])
                    true (concat (map :absolute-path files))))
            ret (apply shell/sh args)]
        (println (:out ret))))))
