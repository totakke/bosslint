(ns bosslint.linter.eastwood
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.util :as util]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [io.aviso.ansi :as ansi]))

(def ^:private excludes
  [#"project.clj$"
   #"data_readers.clj$"])

(defn- path->ns [s]
  (-> s
      (string/replace #"^(src|test)(/cljc?)?/(.*)\.cljc?$" "$3")
      (string/replace #"_" "-")
      (string/replace #"/" ".")))

(defn- eastwood-clojure
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        ret (shell/sh "clojure"
                      "-Sdeps" (format "{:deps {jonase/eastwood {:mvn/version \"%s\"}}}" version)
                      "-m" "eastwood.lint"
                      (->> (map :git-path files)
                           (map path->ns)
                           (string/join " ")
                           (format "{:namespaces [%s]}")))]
    (println (string/trim-newline (:out ret)))))

(defn- eastwood-lein
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args ["lein"
              "update-in" ":plugins" "conj" (format "[jonase/eastwood \"%s\"]" version)
              "--" "eastwood"
              (->> (map :git-path files)
                   (map path->ns)
                   (string/join " ")
                   (format "{:namespaces [%s]}"))]
        ret (apply shell/sh args)]
    (println (string/trim-newline (:out ret)))))

(deflinter :linter/eastwood
  (name [] "eastwood")

  (files [file-group]
    (->> (linter/select-files file-group [:clj :cljc])
         (remove (fn [{:keys [git-path]}]
                   (some #(re-find % git-path) excludes)))))

  (lint [files conf]
    (if-let [f (cond
                 (and (util/command-exists? "clojure")
                      (linter/clojure-project?))
                 eastwood-clojure

                 (and (util/command-exists? "lein")
                      (linter/leiningen-project?))
                 eastwood-lein)]
      (f files conf)
      (println (ansi/yellow "Command not found: clojure or lein")))))
