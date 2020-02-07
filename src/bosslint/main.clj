(ns bosslint.main
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string])
  (:gen-class))

(def excludes
  [#"project.clj$"])

(defn command-exists? [command]
  (try
    (shell/sh command)
    true
    (catch java.io.IOException e
      false)))

(defn check-command [command]
  (when-not (command-exists? command)
    (throw (ex-info (str "Command not found: " command) {}))))

(defn git-diff [ref]
  (check-command "git")
  (->> (shell/sh "git" "diff" "--name-only" "--diff-filter=AMRTU" ref)
       :out
       string/split-lines
       (remove (fn [s] (some #(re-find % s) excludes)))))

(defn path->type [s]
  (condp re-find s
    #"clj$"     :clj
    #"cljc$"    :cljc
    #"cljs$"    :cljs
    #"s[ac]ss$" :sass
    :other))

(defn path->ns [s]
  (-> s
      (string/replace #"^(src|test)(/cljc?)?/(.*)\.cljc?$" "$3")
      (string/replace #"_" "-")
      (string/replace #"/" ".")))

(defn- select-files
  [files types]
  (if (= files :all)
    :all
    (mapcat #(get files %) types)))

(defn cljfmt [files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (or (= files :all) (seq files))
      (newline)
      (println "cljfmt:")
      (check-command "lein")
      (let [ret (if (= files :all)
                  (shell/sh "lein" "cljfmt" "check")
                  (apply shell/sh "lein" "cljfmt" "check" files))]
        (println (:out ret))
        (println (:err ret))))))

(defn clj-kondo [files]
  (let [files (select-files files [:clj :cljc :cljs])]
    (when (or (= files :all) (seq files))
      (newline)
      (println "clj-kondo:")
      (check-command "clj-kondo")
      (let [files (if (= files :all)
                    ["src" "test"]
                    files)
            ret (apply shell/sh "clj-kondo" "--lint" files)]
        (println (:out ret))))))

(defn eastwood [files]
  (let [files (select-files files [:clj :cljc])]
    (when (or (= files :all) (seq files))
      (newline)
      (println "eastwood:")
      (check-command "lein")
      (let [ret (if (= files :all)
                  (shell/sh "lein" "eastwood")
                  (shell/sh "lein" "eastwood"
                            (->> (map path->ns files)
                                 (string/join " ")
                                 (format "{:namespaces [%s]}"))))]
        (println (:out ret))))))

(defn stylelint [files]
  (let [files (select-files files [:css :sass])]
    (when (or (= files :all) (seq files))
      (newline)
      (println "stylelint:")
      (check-command "stylelint")
      (let [ret (if (= files :all)
                  (shell/sh "stylelint" "src/**/*.*[ac]ss")
                  (apply shell/sh "stylelint" files))]
        (println (:out ret))))))

(defn run [ref]
  (let [files (if ref
                (group-by path->type (git-diff ref))
                :all)]
    (println "Files:" files)
    (cljfmt files)
    (clj-kondo files)
    (eastwood files)
    (stylelint files)))

(defn -main [& args]
  (try
    (run (first args))
    (System/exit 0)
    (catch Exception e
      (println (.getMessage e))
      (System/exit 1))))
