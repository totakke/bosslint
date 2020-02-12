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

(defmulti lint (fn [key files] key))

(defmethod lint ::checkstyle
  [_ files]
  (let [files (select-files files [:java])]
    (when (seq files)
      (newline)
      (println "checkstyle:")
      (util/check-command "checkstyle")
      (let [ret (apply shell/sh "checkstyle" "-c" "google_checks.xml" files)]
        (println (:out ret))))))

(defmethod lint ::cljfmt
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (and (.exists (io/file "project.clj"))
               (seq files))
      (newline)
      (println "cljfmt:")
      (util/check-command "lein")
      (let [ret (apply shell/sh "lein" "cljfmt" "check" files)]
        (println (:out ret))
        (println (:err ret))))))

(defmethod lint ::clj-kondo
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (seq files)
      (newline)
      (println "clj-kondo:")
      (util/check-command "clj-kondo")
      (let [ret (apply shell/sh "clj-kondo" "--lint" files)]
        (println (:out ret))))))

(defmethod lint ::eastwood
  [_ files]
  (let [files (select-files files [:clj :cljc])]
    (when (and (.exists (io/file "project.clj"))
               (seq files))
      (newline)
      (println "eastwood:")
      (util/check-command "lein")
      (let [ret (shell/sh "lein" "eastwood"
                          (->> (map path->ns files)
                               (string/join " ")
                               (format "{:namespaces [%s]}")))]
        (println (:out ret))))))

(defmethod lint ::stylelint
  [_ files]
  (let [files (select-files files [:css :sass])]
    (when (seq files)
      (newline)
      (println "stylelint:")
      (util/check-command "stylelint")
      (let [ret (apply shell/sh "stylelint" files)]
        (println (:out ret))))))
