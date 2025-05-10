(ns bosslint.linter.cljfmt
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]
            [io.aviso.ansi :as ansi]))

(def ^:private cljfmt-artifact "dev.weavejester/cljfmt")

(defn- cljfmt-cli
  [files conf]
  (let [args (concat ["cljfmt" "check"]
                     (:command-options conf)
                     (map :absolute-path files))]
    (case (apply process/run args)
      0 :success
      1 :warning
      :error)))

(defn- cljfmt-clojure
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args (concat ["clojure"
                      "-Sdeps" (format "{:deps {%s {:mvn/version \"%s\"}}}"
                                       cljfmt-artifact version)
                      "-M" "-m" "cljfmt.main" "check"]
                     (or (:command-options conf)
                         (:command-options (:clojure conf)))
                     (map :absolute-path files))]
    (case (apply process/run args)
      0 :success
      1 :warning
      :error)))

(defn- cljfmt-lein
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args (concat ["lein"
                      "update-in" ":plugins" "conj" (format "[%s \"%s\"]"
                                                            cljfmt-artifact version)
                      "--" "cljfmt" "check"]
                     (map :git-path files))]
    (case (apply process/run args)
      0 :success
      1 :warning
      :error)))

(deflinter :linter/cljfmt
  (name [] "cljfmt")

  (files [file-group]
    (linter/select-files file-group [:clj :cljc :cljs]))

  (lint [{:keys [files]} conf]
    (if-let [f (cond
                 (process/command-exists? "cljfmt")
                 cljfmt-cli

                 (and (linter/leiningen-project?)
                      (process/command-exists? "lein"))
                 cljfmt-lein

                 (process/command-exists? "clojure")
                 cljfmt-clojure)]
      (f files conf)
      (println (ansi/yellow "Command not found: clojure or lein")))))
