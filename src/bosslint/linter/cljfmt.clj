(ns bosslint.linter.cljfmt
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.util :as util]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [io.aviso.ansi :as ansi]))

(defn- cljfmt-clojure
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args (concat ["clojure"
                      "-Sdeps" (format "{:deps {cljfmt/cljfmt {:mvn/version \"%s\"}}}" version)
                      "-M" "-m" "cljfmt.main" "check"]
                     (:command-options (:clojure conf))
                     (map :absolute-path files))
        ret (apply shell/sh args)]
    (when-not (string/blank? (:out ret))
      (println (string/trim-newline (:out ret))))
    (when-not (string/blank? (:err ret))
      (println (string/trim-newline (:err ret))))))

(defn- cljfmt-lein
  [files conf]
  (let [version (or (:version conf) "RELEASE")
        args (concat ["lein"
                      "update-in" ":plugins" "conj" (format "[lein-cljfmt \"%s\"]" version)
                      "--" "cljfmt" "check"]
                     (map :git-path files))
        ret (apply shell/sh args)]
    (when-not (string/blank? (:out ret))
      (println (string/trim-newline (:out ret))))
    (when-not (string/blank? (:err ret))
      (println (string/trim-newline (:err ret))))))

(deflinter :linter/cljfmt
  (name [] "cljfmt")

  (files [file-group]
    (linter/select-files file-group [:clj :cljc :cljs]))

  (lint [files conf]
    (if-let [f (cond
                 (and (linter/leiningen-project?)
                      (util/command-exists? "lein"))
                 cljfmt-lein

                 (util/command-exists? "clojure")
                 cljfmt-clojure)]
      (f files conf)
      (println (ansi/yellow "Command not found: clojure or lein")))))
