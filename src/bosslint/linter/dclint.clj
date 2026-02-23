(ns bosslint.linter.dclint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]
            [io.aviso.ansi :as ansi]))

(defn- dclint-command []
  (cond
    (process/command-exists? "dclint") ["dclint"]
    (process/command-exists? "npx") ["npx" "dclint"]
    :else nil))

(deflinter :linter/dclint
  (name [] "Docker Compose Linter")

  (files [file-group]
    (linter/select-files file-group [:docker-compose]))

  (lint [{:keys [files]} conf]
    (if-let [command (dclint-command)]
      (let [args (concat command
                         (:command-options conf)
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error))
      (println (ansi/yellow "Command not found: dclint or npx")))))
