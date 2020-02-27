(ns bosslint.main
  (:require [bosslint.linters :as linters]
            [bosslint.util :as util]
            [clj-sub-command.core :as cmd]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def version "0.1.0-SNAPSHOT")

(def linters
  [::linters/checkstyle
   ::linters/cljfmt
   ::linters/clj-kondo
   ::linters/eastwood
   ::linters/hadolint
   ::linters/stylelint])

(def excludes
  [#"project.clj$"
   #"data_readers.clj$"])

(defn git-diff [ref]
  (util/check-command "git")
  (->> (shell/sh "git" "diff" "--name-only" "--diff-filter=AMRTU" ref)
       :out
       string/split-lines
       (remove (fn [s] (some #(re-find % s) excludes)))))

(defn git-ls-files []
  (util/check-command "git")
  (->> (shell/sh "git" "ls-files" "--full-name")
       :out
       string/split-lines
       (remove (fn [s] (some #(re-find % s) excludes)))))

(defn git-top-dir []
  (util/check-command "git")
  (-> (shell/sh "git" "rev-parse" "--show-toplevel")
      :out
      string/trim-newline))

(defn path->type [s]
  (condp re-find s
    #"\.clj$"                 :clj
    #"\.cljc$"                :cljc
    #"\.cljs$"                :cljs
    #"Dockerfile(\.[-\w]+)?$" :docker
    #"\.java$"                :java
    #"\.s[ac]ss$"             :sass
    :other))

(defn enum-files [ref]
  (let [top-dir (git-top-dir)]
    (->> (if ref
           (git-diff ref)
           (git-ls-files))
         (map (fn [s]
                {:git-path s
                 :absolute-path (str top-dir "/" s)}))
         (group-by (comp path->type :git-path)))))

(defn run-check
  [ref options]
  (let [files (enum-files ref)
        enabled-linter? (if (:linter options)
                          (set (map #(keyword "bosslint.linters" %) (:linter options)))
                          (constantly true))]
    (doseq [l (filter enabled-linter? linters)]
      (linters/lint l files))))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;;; check

(def check-cmd-options
  [["-l" "--linter LINTER" "Select linter"
    :assoc-fn (fn [m k v] (update m k #(conj (or % []) v)))
    :validate [(set (map name linters))]]
   ["-h" "--help" "Print help"]])

(defn check-cmd-usage [options-summary]
  (->> ["Usage: bosslint check [<options>] [<commit>]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn validate-check-cmd-args [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args check-cmd-options)]
    (cond
      (:version options)
      {:exit-message version :ok? true}

      (:help options)
      {:exit-message (check-cmd-usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      (<= (count arguments) 1)
      {:ref (first arguments) :options options}

      :else
      {:exit-message (check-cmd-usage summary)})))

(defn check-cmd [args]
  (let [{:keys [ref options exit-message ok?]} (validate-check-cmd-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (run-check ref options))))

;;; linters

(defn linters-cmd [args]
  (doseq [l linters]
    (println (name l))))

;;; main

(def options
  [["-h" "--help" "Print help"]
   ["-v" "--version" "Print version"]])

(def commands
  [["check" "Check files"]
   ["linters" "Show linters"]])

(defn usage [options-summary commands-summary]
  (->> ["Usage: bosslint [--help] [--version] <command> [<args>]"
        ""
        "Options:"
        options-summary
        ""
        "Commands:"
        commands-summary]
       (string/join \newline)))

(defn validate-args [args]
  (let [{:keys [options command arguments errors options-summary commands-summary]}
        (cmd/parse-cmds args options commands :allow-empty-command true)]
    (cond
      (:version options)
      {:exit-message version, :ok? true}

      (:help options)
      {:exit-message (usage options-summary commands-summary), :ok? true}

      errors
      {:exit-message (error-msg errors)}

      command
      {:command command, :arguments arguments}

      :else
      {:exit-message (usage options-summary commands-summary)})))

(defn -main [& args]
  (let [{:keys [command arguments exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (try
        (case command
          :check   (check-cmd arguments)
          :linters (linters-cmd arguments))
        (System/exit 0)
        (catch Exception e
          (exit 1 (.getMessage e)))))))
