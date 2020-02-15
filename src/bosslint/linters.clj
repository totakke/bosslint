(ns bosslint.linters
  (:require [bosslint.util :as util]
            [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn- path->ns [s]
  (-> s
      (string/replace #"^(src|test)(/cljc?)?/(.*)\.cljc?$" "$3")
      (string/replace #"_" "-")
      (string/replace #"/" ".")))

(defn- select-files
  [files types]
  (mapcat #(get files %) types))

(defn- print-files
  [files]
  (->> (map :git-path files)
       (string/join \newline)
       println))

(defmulti lint (fn [key files] key))

(defmethod lint ::checkstyle
  [_ files]
  (let [files (select-files files [:java])]
    (when (seq files)
      (newline)
      (println "checkstyle:")
      (print-files files)
      (util/check-command "checkstyle")
      (let [ret (apply shell/sh "checkstyle" "-c" "google_checks.xml"
                       (map :absolute-path files))]
        (println (:out ret))))))

(defmethod lint :bosslint.linters.cljfmt/clojure
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (seq files)
      (newline)
      (println "cljfmt (clojure):")
      (print-files files)
      (util/check-command "clojure")
      (let [ret (apply shell/sh "clojure"
                       "-Sdeps" "{:deps {cljfmt {:mvn/version \"0.6.4\"}}}"
                       "-m" "cljfmt.main" "check"
                       (map :absolute-path files))]
        (println (:out ret))
        (println (:err ret))))))

(defmethod lint :bosslint.linters.cljfmt/lein
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (and (seq files)
               (util/command-exists? "lein")
               (zero? (:exit (shell/sh "lein" "deps" ":tree")))
               (not= (:out (shell/sh "lein" "cljfmt" "-h"))
                     "Task: 'cljfmt' not found"))
      (newline)
      (println "cljfmt (lein):")
      (print-files files)
      (let [ret (apply shell/sh "lein" "cljfmt" "check"
                       (map :git-path files))]
        (println (:out ret))
        (println (:err ret))))))

(defmethod lint ::cljfmt
  [_ files]
  (when (seq (select-files files [:clj :cljc :cljs]))
    (when-let [key (cond
                     (util/command-exists? "clojure") :bosslint.linters.cljfmt/clojure
                     (util/command-exists? "lein") :bosslint.linters.cljfmt/lein)]
      (lint key files))))

(defmethod lint ::clj-kondo
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (seq files)
      (newline)
      (println "clj-kondo:")
      (print-files files)
      (util/check-command "clj-kondo")
      (let [ret (apply shell/sh "clj-kondo" "--lint"
                       (map :absolute-path files))]
        (println (:out ret))))))

(defmethod lint :bosslint.linters.eastwood/clojure
  [_ files]
  (let [files (select-files files [:clj :cljc])]
    (when (seq files)
      (newline)
      (println "eastwood (clojure)")
      (print-files files)
      (let [ret (shell/sh "clojure"
                          "-Sdeps" "{:deps {jonase/eastwood {:mvn/version \"RELEASE\"}}}"
                          "-m" "eastwood.lint"
                          (->> (map :git-path files)
                               (map path->ns)
                               (string/join " ")
                               (format "{:namespaces [%s]}")))]
        (println (:out ret))))))

(defmethod lint :bosslint.linters.eastwood/lein
  [_ files]
  (let [files (select-files files [:clj :cljc])]
    (when (and (seq files)
               (util/command-exists? "lein")
               (zero? (:exit (shell/sh "lein" "deps" ":tree")))
               (zero? (:exit (shell/sh "lein" "eastwood" "help"))))
      (newline)
      (println "eastwood (lein):")
      (print-files files)
      (let [ret (shell/sh "lein" "eastwood"
                          (->> (map :git-path files)
                               (map path->ns)
                               (string/join " ")
                               (format "{:namespaces [%s]}")))]
        (println (:out ret))))))

(defn- clojure-project? []
  (let [{:keys [config-files]} (-> (shell/sh "clojure" "-Sdescribe")
                                   :out
                                   edn/read-string)]
    (some? ((set config-files) "deps.edn"))))

(defn- leiningen-project? []
  (zero? (:exit (shell/sh "lein" "deps" ":tree"))))

(defmethod lint ::eastwood
  [_ files]
  (when (seq (select-files files [:clj :cljc]))
    (when-let [key (cond
                     (and (util/command-exists? "clojure")
                          (clojure-project?))
                     :bosslint.linters.eastwood/clojure

                     (and (util/command-exists? "lein")
                          (leiningen-project?))
                     :bosslint.linters.eastwood/lein)]
      (lint key files))))

(defmethod lint ::hadolint
  [_ files]
  (let [files (select-files files [:docker])]
    (when (seq files)
      (newline)
      (println "hadolint:")
      (print-files files)
      (let [ret (apply shell/sh "hadolint" (map :absolute-path files))]
        (println (:out ret))))))

(defmethod lint ::stylelint
  [_ files]
  (let [files (select-files files [:css :sass])]
    (when (seq files)
      (newline)
      (println "stylelint:")
      (print-files files)
      (util/check-command "stylelint")
      (let [ret (apply shell/sh "stylelint" (map :absolute-path files))]
        (println (:out ret))))))
