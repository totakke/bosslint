(ns bosslint.linter
  (:refer-clojure :exclude [name])
  (:require [bosslint.process :as process]
            [clojure.edn :as edn]
            [io.aviso.ansi :as ansi]))

(def ^:dynamic *verbose?* false)

(defmulti name identity)

(defmulti files (fn [key file-group] key))

(defmulti lint (fn [key files config] key))

(defmacro deflinter
  {:style/indent [1 :form [:defn]]}
  [key & methods]
  (let [defmethods (for [[sym args & body] methods]
                     `(defmethod ~(symbol "bosslint.linter" (str sym)) ~key
                        ~(into ['_] args)
                        ~@body))]
    `(do
       (derive ~key :bosslint/linter)
       ~@defmethods)))

(defn select-files
  [file-group types]
  (mapcat #(get file-group %) types))

(defn check-command
  [command]
  (or (process/command-exists? command)
      (println (ansi/yellow (str command " not found")))))

(defn clojure-project? []
  (try
    (let [{:keys [config-files]} (edn/read-string
                                  (process/exec {:err :discard}
                                                "clojure" "-Sdescribe"))]
      (some? ((set config-files) "deps.edn")))
    (catch RuntimeException _
      false)))

(defn leiningen-project? []
  (try
    (process/exec {:err :discard} "lein" "deps" ":tree")
    true
    (catch RuntimeException _
      false)))
