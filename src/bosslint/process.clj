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
    (process/exec "command" "-v" command)
    true
    (catch RuntimeException _
      false)))

(def command-exists? (memoize command-exists?*))
