(ns bosslint.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn config-dir []
  (io/file (System/getProperty "user.home") ".bosslint"))

(def config-filename "config.edn")

(defn load-config
  ([]
   (let [file (io/file (config-dir) config-filename)]
     (if (.exists file)
       (load-config file)
       {})))
  ([file]
   (edn/read-string (slurp file))))

(defn resolve-path [s]
  (when s
    (let [file (io/file (config-dir) s)]
      (if (.exists file)
        (.getAbsolutePath file)
        (throw (ex-info (str "File not found:" s) {}))))))
