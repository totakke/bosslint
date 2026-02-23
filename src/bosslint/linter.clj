(ns bosslint.linter
  (:refer-clojure :exclude [name])
  (:require [bosslint.process :as process]
            [clojure.edn :as edn]
            [io.aviso.ansi :as ansi]))

(def ^:dynamic *verbose?* false)

(defmulti name identity)

(defmulti files (fn [key file-group] key))

(defmulti lint (fn [key diff config] key))

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

(def ^:private path-type-pairs
  {#"\.clj$" :clj
   #"\.cljc$" :cljc
   #"\.cljs$" :cljs
   #"\.css$" :css
   #"\.dart$" :dart
   #"(^|/)Dockerfile(\.[-\w]+)?$" :docker
   #"(^|/)(docker-)?compose(\.[-\w]+)?\.ya?ml$" :docker-compose
   #"(^|/)\.env$" :dot-env
   #"\.java$" :java
   #"\.json$" :json
   #"\.(md|markdown)$" :markdown
   #"\.py$" :python
   #"\.s[ac]ss$" :sass
   #"\.sh$" :shell
   #"\.sql$" :sql
   #"\.swift$" :swift
   #"\.tf$" :terraform
   #"^\.github/workflows/.+\.ya?ml$" :workflow
   #"\.ya?ml$" :yaml})

(def ^:private file-type-set
  (set (vals path-type-pairs)))

(defn path->types [s]
  (let [types (->> path-type-pairs
                   (filter #(re-find (first %) s))
                   (map second))]
    (if (seq types)
      (set types)
      #{:other})))

(defn select-files
  [file-group types]
  {:pre [(every? file-type-set types)]}
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
