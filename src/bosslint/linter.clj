(ns bosslint.linter
  (:refer-clojure :exclude [name])
  (:require [bosslint.util :as util]
            [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [io.aviso.ansi :as ansi]))

(defmulti name identity)

(defmulti files (fn [key diff-files] key))

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
  [files types]
  (mapcat #(get files %) types))

(defn print-files
  [files]
  (->> (map :git-path files)
       (string/join \newline)
       println))

(defn check-command
  [command]
  (or (util/command-exists? command)
      (println (ansi/yellow (str command " not found")))))

(defn clojure-project? []
  (let [{:keys [config-files]} (-> (shell/sh "clojure" "-Sdescribe")
                                   :out
                                   edn/read-string)]
    (some? ((set config-files) "deps.edn"))))

(defn leiningen-project? []
  (zero? (:exit (shell/sh "lein" "deps" ":tree"))))
