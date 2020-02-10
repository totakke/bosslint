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
  (if (= files :all)
    :all
    (mapcat #(get files %) types)))

(defmulti lint (fn [key files] key))

(defmethod lint ::cljfmt
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (and (.exists (io/file "project.clj"))
               (or (= files :all) (seq files)))
      (newline)
      (println "cljfmt:")
      (util/check-command "lein")
      (let [ret (if (= files :all)
                  (shell/sh "lein" "cljfmt" "check")
                  (apply shell/sh "lein" "cljfmt" "check" files))]
        (println (:out ret))
        (println (:err ret))))))

(defmethod lint ::clj-kondo
  [_ files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (or (= files :all) (seq files))
      (newline)
      (println "clj-kondo:")
      (util/check-command "clj-kondo")
      (let [files (if (= files :all)
                    ["src" "test"]
                    files)
            ret (apply shell/sh "clj-kondo" "--lint" files)]
        (println (:out ret))))))

(defmethod lint ::eastwood
  [_ files]
  (let [files (select-files files [:clj :cljc])]
    (when (and (.exists (io/file "project.clj"))
               (or (= files :all) (seq files)))
      (newline)
      (println "eastwood:")
      (util/check-command "lein")
      (let [ret (if (= files :all)
                  (shell/sh "lein" "eastwood")
                  (shell/sh "lein" "eastwood"
                            (->> (map path->ns files)
                                 (string/join " ")
                                 (format "{:namespaces [%s]}"))))]
        (println (:out ret))))))

(defmethod lint ::stylelint
  [_ files]
  (let [files (select-files files [:css :sass])]
    (when (or (= files :all) (seq files))
      (newline)
      (println "stylelint:")
      (util/check-command "stylelint")
      (let [ret (if (= files :all)
                  (shell/sh "stylelint" "src/**/*.*[ac]ss")
                  (apply shell/sh "stylelint" files))]
        (println (:out ret))))))
