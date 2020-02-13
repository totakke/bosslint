(ns bosslint.linters
  (:require [bosslint.util :as util]
            [clojure.java.io :as io]
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

(defmethod lint ::cljfmt
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (and (util/command-exists? "lein")
               (zero? (:exit (shell/sh "lein" "deps" ":tree")))
               (seq files))
      (newline)
      (println "cljfmt:")
      (print-files files)
      (let [ret (apply shell/sh "lein" "cljfmt" "check"
                       (map :git-path files))]
        (println (:out ret))
        (println (:err ret))))))

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

(defmethod lint ::eastwood
  [_ files]
  (let [files (select-files files [:clj :cljc])]
    (when (and (util/command-exists? "lein")
               (zero? (:exit (shell/sh "lein" "deps" ":tree")))
               (seq files))
      (newline)
      (println "eastwood:")
      (print-files files)
      (let [ret (shell/sh "lein" "eastwood"
                          (->> (map :git-path files)
                               (map path->ns)
                               (string/join " ")
                               (format "{:namespaces [%s]}")))]
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
