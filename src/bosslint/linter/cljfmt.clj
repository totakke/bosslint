(ns bosslint.linter.cljfmt
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.util :as util]
            [clojure.java.shell :as shell]))

(defn- cljfmt-clojure
  [files]
  (when (linter/check-command "clojure")
    (let [ret (apply shell/sh "clojure"
                     "-Sdeps" "{:deps {cljfmt {:mvn/version \"0.6.4\"}}}"
                     "-m" "cljfmt.main" "check"
                     (map :absolute-path files))]
      (println (:out ret))
      (println (:err ret)))))

(defn- cljfmt-lein
  [files]
  (when (and (util/command-exists? "lein")
             (zero? (:exit (shell/sh "lein" "deps" ":tree")))
             (not= (:out (shell/sh "lein" "cljfmt" "-h"))
                   "Task: 'cljfmt' not found"))
    (let [ret (apply shell/sh "lein" "cljfmt" "check"
                     (map :git-path files))]
      (println (:out ret))
      (println (:err ret)))))

(deflinter :linter/cljfmt
  (name [] "cljfmt")

  (files [diff-files]
    (linter/select-files diff-files [:clj :cljc :cljs]))

  (lint [files]
    (when-let [f (cond
                   (and (linter/leiningen-project?)
                        (util/command-exists? "lein"))
                   cljfmt-lein

                   (util/command-exists? "clojure")
                   cljfmt-clojure)]
      (f files))))
