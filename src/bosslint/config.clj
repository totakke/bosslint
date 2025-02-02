(ns bosslint.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import java.io.File))

(def config-filename "config.edn")

(defn find-config-file
  []
  (->> [(io/file (System/getenv "XDG_CONFIG_HOME") "bosslint")
        (io/file (System/getProperty "user.home") ".config" "bosslint")
        (io/file (System/getProperty "user.home") ".bosslint")]
       (map #(io/file % config-filename))
       (filter File/.isFile)
       first))

(defn load-config
  ([]
   (if-let [file (find-config-file)]
     (load-config file)
     {}))
  ([file]
   (edn/read-string (slurp file))))
