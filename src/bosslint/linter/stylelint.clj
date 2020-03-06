(ns bosslint.linter.stylelint
  (:require [bosslint.config :as config]
            [bosslint.linter :as linter :refer [deflinter]]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(deflinter :linter/stylelint
  (name [] "stylelint")

  (files [file-group]
    (linter/select-files file-group [:css :sass]))

  (lint [files conf]
    (when (linter/check-command "stylelint")
      (let [opt-config (config/resolve-path (:config conf))
            args (flatten
                  (cond-> ["stylelint"]
                    opt-config (concat ["--config" opt-config])
                    true (concat (map :absolute-path files))))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
