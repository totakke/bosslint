(ns bosslint.linter.eastwood
  (:require [bosslint.git :as git]
            [bosslint.linter :as linter :refer [deflinter]]
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
  [files additional-options]
  (pr-str
   (assoc (or additional-options {})
          :namespaces (->> (map :git-path files)
                           (keep path->ns)
                           (map symbol)))))

(defn- aliases-with-extra-paths
  [dir]
  (->> (deps/slurp-deps (io/file dir "deps.edn"))
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
  [files conf dir]
  (let [version (or (:version conf) "RELEASE")
        aliases (concat (aliases-with-extra-paths dir) [":eastwood"])]
    (if (zero? (process/run "clojure"
                            "-Sdeps" (clojure-sdeps version)
                            (str "-M" (string/join aliases))
                            (eastwood-options files (:options conf))))
      :success
      :error)))

(defn- eastwood-lein
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args ["lein"
              "update-in" ":plugins" "conj" (format "[jonase/eastwood \"%s\"]" version)
              "--" "eastwood"
              (eastwood-options files (:options conf))]]
    (if (zero? (apply process/run args))
      :success
      :error)))

(deflinter :linter/eastwood
  (name [] "Eastwood")

  (files [file-group]
    (->> (linter/select-files file-group [:clj :cljc])
         (remove (fn [{:keys [git-path]}]
                   (some #(re-find % git-path) excludes)))))

  (lint [files conf]
    (let [top-dir (git/top-dir)]
      (binding [process/*working-directory* top-dir]
        (if-let [f (cond
                     (and (process/command-exists? "lein")
                          (linter/leiningen-project?))
                     eastwood-lein

                     (and (process/command-exists? "clojure")
                          (linter/clojure-project?))
                     #(eastwood-clojure %1 %2 top-dir))]
          (f files conf)
          (println (ansi/yellow "Command not found: clojure or lein")))))))
