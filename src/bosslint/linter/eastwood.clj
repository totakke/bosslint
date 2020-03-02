(ns bosslint.linter.eastwood
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.util :as util]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn- path->ns [s]
  (-> s
      (string/replace #"^(src|test)(/cljc?)?/(.*)\.cljc?$" "$3")
      (string/replace #"_" "-")
      (string/replace #"/" ".")))

(defn- eastwood-clojure
  [files]
  (let [ret (shell/sh "clojure"
                      "-Sdeps" "{:deps {jonase/eastwood {:mvn/version \"RELEASE\"}}}"
                      "-m" "eastwood.lint"
                      (->> (map :git-path files)
                           (map path->ns)
                           (string/join " ")
                           (format "{:namespaces [%s]}")))]
    (println (:out ret))))

(defn- eastwood-lein
  [files]
  (when (and (util/command-exists? "lein")
             (zero? (:exit (shell/sh "lein" "deps" ":tree")))
             (zero? (:exit (shell/sh "lein" "eastwood" "help"))))
    (let [ret (shell/sh "lein" "eastwood"
                        (->> (map :git-path files)
                             (map path->ns)
                             (string/join " ")
                             (format "{:namespaces [%s]}")))]
      (println (:out ret)))))

(deflinter :linter/eastwood
  (name [] "eastwood")

  (files [diff-files]
    (linter/select-files diff-files [:clj :cljc]))

  (lint [files]
    (when-let [f (cond
                   (and (util/command-exists? "clojure")
                        (linter/clojure-project?))
                   eastwood-clojure

                   (and (util/command-exists? "lein")
                        (linter/leiningen-project?))
                   eastwood-lein)]
      (f files))))
