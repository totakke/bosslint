(ns bosslint.linter.cljfmt
  (:require [bosslint.config :as config]
            [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.util :as util]
            [clojure.java.shell :as shell]))

(defn- cljfmt-clojure
  [files conf]
  (when (linter/check-command "clojure")
    (let [opt-indents (config/resolve-path (:indents conf))
          args (flatten
                (cond-> ["clojure"
                         "-Sdeps" "{:deps {cljfmt {:mvn/version \"0.6.4\"}}}"
                         "-m" "cljfmt.main" "check"]
                  opt-indents (concat ["--indents" opt-indents])
                  true (concat (map :absolute-path files))))
          ret (apply shell/sh args)]
      (println (:out ret))
      (println (:err ret)))))

(defn- cljfmt-lein
  [files _]
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

  (lint [files conf]
    (when-let [f (cond
                   (and (linter/leiningen-project?)
                        (util/command-exists? "lein"))
                   cljfmt-lein

                   (util/command-exists? "clojure")
                   cljfmt-clojure)]
      (f files conf))))
