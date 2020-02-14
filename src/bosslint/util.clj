(ns bosslint.util
  (:require [clojure.java.shell :as shell]))

(defn command-exists?* [command]
  (try
    (shell/sh command)
    true
    (catch java.io.IOException _
      false)))

(def command-exists? (memoize command-exists?*))

(defn check-command [command]
  (when-not (command-exists? command)
    (throw (ex-info (str "Command not found: " command) {}))))
