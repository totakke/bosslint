(ns bosslint.main
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
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
    (when (and (.exists (io/file "project.clj"))
               (or (= files :all) (seq files)))
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
    (when (and (.exists (io/file "project.clj"))
               (or (= files :all) (seq files)))
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

(def cli-options
  [["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Usage: bosslint [<options>] [<commit>]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      (<= (count arguments) 1)
      {:ref (first arguments) :options options}

      :else
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [ref options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (try
        (run ref)
        (System/exit 0)
        (catch Exception e
          (exit 1 (.getMessage e)))))))
