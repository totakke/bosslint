(ns bosslint.process
  (:require [clojure.java.process :as process])
  (:import [java.util.concurrent TimeUnit]))

(def ^:dynamic *working-directory* ".")

(defn start
  [& opts+args]
  (let [[opts command] (if (map? (first opts+args))
                         [(first opts+args) (rest opts+args)]
                         [{} opts+args])
        opts (merge {:out :inherit
                     :err :inherit
                     :dir *working-directory*}
                    opts)]
    (apply process/start opts command)))

(defn run
  [& opts+args]
  (let [proc (apply start opts+args)]
    @(process/exit-ref proc)))

(defn exec
  [& opts+args]
  (let [[opts command] (if (map? (first opts+args))
                         [(first opts+args) (rest opts+args)]
                         [{} opts+args])
        opts (merge {:dir *working-directory*} opts)]
    (apply process/exec opts command)))

(defn command-exists?*
  [command]
  (try
    (let [proc (process/start command)]
      (.waitFor proc 1000 TimeUnit/MILLISECONDS)
      true)
    (catch java.io.IOException _
      false)))

(def command-exists? (memoize command-exists?*))
