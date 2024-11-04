(ns bosslint.linter.eastwood
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.deps :as deps]
            [io.aviso.ansi :as ansi]))

(def ^:private excludes
  [#"project.clj$"
   #"data_readers.clj$"])

(def ^:private path-res
  [#"^(?:src|test)/main/clojure/(.*)\.cljc?$"
   #"^(?:src|test|dev)/(?:cljc?/)?(.*)\.cljc?$"])

(defn- path->ns
  [path]
  (when-let [[_ s] (some #(re-find % path) path-res)]
    (-> s
        (string/replace #"_" "-")
        (string/replace #"/" "."))))

(defn- eastwood-options
  [files]
  (->> (map :git-path files)
       (map path->ns)
       (string/join " ")
       (format "{:namespaces [%s]}")))

(defn- aliases-with-extra-paths
  []
  (->> (deps/slurp-deps (io/file "deps.edn"))
       :aliases
       (filter #(contains? (second %) :extra-paths))
       keys
       (map str)))

(defn- clojure-sdeps
  [eastwood-version]
  (format "{:aliases
 {:eastwood
  {:extra-deps {jonase/eastwood {:mvn/version \"%s\"}}
   :main-opts [\"-m\" \"eastwood.lint\"]}}}"
          eastwood-version))

(defn- eastwood-clojure
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        aliases (concat (aliases-with-extra-paths) [":eastwood"])]
    (process/run "clojure"
                 "-Sdeps" (clojure-sdeps version)
                 (str "-M" (string/join aliases))
                 (eastwood-options files))))

(defn- eastwood-lein
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args ["lein"
              "update-in" ":plugins" "conj" (format "[jonase/eastwood \"%s\"]" version)
              "--" "eastwood"
              (eastwood-options files)]]
    (apply process/run args)))

(deflinter :linter/eastwood
  (name [] "eastwood")

  (files [file-group]
    (->> (linter/select-files file-group [:clj :cljc])
         (remove (fn [{:keys [git-path]}]
                   (some #(re-find % git-path) excludes)))))

  (lint [files conf]
    (if-let [f (cond
                 (and (process/command-exists? "clojure")
                      (linter/clojure-project?))
                 eastwood-clojure

                 (and (process/command-exists? "lein")
                      (linter/leiningen-project?))
                 eastwood-lein)]
      (f files conf)
      (println (ansi/yellow "Command not found: clojure or lein")))))
