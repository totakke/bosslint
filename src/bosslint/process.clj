(ns bosslint.process
  (:require [clojure.java.process :as process])
  (:import [java.util.concurrent TimeUnit]))

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
    (let [proc (process/start command)]
      (.waitFor proc 1000 TimeUnit/MILLISECONDS)
      true)
    (catch java.io.IOException _
      false)))

(def command-exists? (memoize command-exists?*))
