(ns bosslint.util
  (:require [clojure.java.shell :as shell]))

(defn command-exists?* [command]
  (try
    (shell/sh command)
    true
    (catch java.io.IOException _
      false)))

(def command-exists? (memoize command-exists?*))
