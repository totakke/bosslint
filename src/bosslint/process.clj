(ns bosslint.process
  (:require [clojure.java.process :as process]))

(defn run
  [& args]
  (let [proc (apply process/start
                    {:out :inherit
                     :err :inherit}
                    args)]
    @(process/exit-ref proc)))

(defn command-exists?*
  [command]
  (try
    (process/exec command)
    true
    (catch java.io.IOException _
      false)))

(def command-exists? (memoize command-exists?*))
