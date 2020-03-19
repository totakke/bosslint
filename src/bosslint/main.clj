(ns bosslint.main
  (:require [bosslint.config :as config]
            [bosslint.linter :as linter]
            (bosslint.linter checkstyle clj-kondo cljfmt dartanalyzer eastwood
                             flake8 hadolint stylelint swiftlint yamllint)
            [bosslint.util :as util]
            [clj-sub-command.core :as cmd]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [io.aviso.ansi :as ansi])
  (:gen-class))

(def version "0.2.0-SNAPSHOT")

(defn- assert-command [command]
  (when-not (util/command-exists? command)
    (throw (ex-info (str "Command not found: " command) {}))))

(defn git-diff
  [ref1 ref2]
  (assert-command "git")
  (let [args (cond-> ["git" "diff" "--name-only" "--diff-filter=AMRTU"]
               ref1 (conj ref1)
               ref2 (conj ref2))]
    (->> (apply shell/sh args)
         :out
         string/split-lines)))

(defn git-ls-files []
  (assert-command "git")
  (->> (shell/sh "git" "ls-files" "--full-name")
       :out
       string/split-lines))

(defn git-top-dir []
  (assert-command "git")
  (-> (shell/sh "git" "rev-parse" "--show-toplevel")
      :out
      string/trim-newline))

(defn path->type [s]
  (condp re-find s
    #"\.clj$"                 :clj
    #"\.cljc$"                :cljc
    #"\.cljs$"                :cljs
    #"\.dart$"                :dart
    #"Dockerfile(\.[-\w]+)?$" :docker
    #"\.java$"                :java
    #"\.py$"                  :python
    #"\.s[ac]ss$"             :sass
    #"\.swift$"               :swift
    #"\.ya?ml$"               :yaml
    :other))

(defn enum-files [ref1 ref2]
  (let [top-dir (git-top-dir)]
    (->> (if (= ref1 ":all")
           (git-ls-files)
           (git-diff ref1 ref2))
         (map (fn [s]
                {:git-path s
                 :absolute-path (str top-dir "/" s)})))))

(defn- desc-files [files]
  (->> (map :git-path files)
       (map #(str "  " %))
       (string/join \newline)))

(defn- desc-linters [linters]
  (->> (map linter/name linters)
       (map #(str "  " %))
       (string/join \newline)))

(defn run-check
  [ref1 ref2 options]
  (binding [linter/*verbose?* (:verbose options)]
    (let [diff-files (enum-files ref1 ref2)
          file-group (group-by (comp path->type :git-path) diff-files)
          enabled-linter? (if (:linter options)
                            (comp (set (:linter options)) linter/name)
                            (constantly true))
          linters (filter enabled-linter? (descendants :bosslint/linter))
          conf (if (:config options)
                 (config/load-config (:config options))
                 (config/load-config))]
      (when linter/*verbose?*
        (->> ["Enabled linters:"
              (desc-linters linters)
              "Diff files:"
              (desc-files diff-files)
              ""]
             (string/join \newline)
             ansi/cyan
             println))
      (doseq [key linters]
        (if-let [files (seq (linter/files key file-group))]
          (do (println (str (ansi/green (linter/name key)) ":"))
              (when linter/*verbose?*
                (->> ["Files:"
                      (desc-files files)]
                     (string/join \newline)
                     ansi/cyan
                     println))
              (linter/lint key files (get conf (keyword (name key))))
              (newline))
          (when linter/*verbose?*
            (println (str (ansi/green (linter/name key)) ":"))
            (println (ansi/cyan "No files"))
            (newline)))))))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;;; check

(def check-cmd-options
  [["-c" "--config CONFIG" "Specify a configuration file (default: $HOME/.bosslint/config.edn)"]
   ["-l" "--linter LINTER" "Select linter"
    :assoc-fn (fn [m k v] (update m k #(conj (or % []) v)))
    :validate [(set (map name (descendants :bosslint/linter)))]]
   ["-v" "--verbose" "Make bosslint verbose during the operation"]
   ["-h" "--help" "Print help"]])

(defn check-cmd-usage [options-summary]
  (->> ["Usage: bosslint check [<options>] [<commit> [<commit>]]"
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

      (<= (count arguments) 2)
      {:ref1 (first arguments) :ref2 (second arguments) :options options}

      :else
      {:exit-message (check-cmd-usage summary)})))

(defn check-cmd [args]
  (let [{:keys [ref1 ref2 options exit-message ok?]} (validate-check-cmd-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (run-check ref1 ref2 options))))

;;; linters

(defn linters-cmd [_]
  (doseq [s (->> (descendants :bosslint/linter)
                 (map linter/name)
                 sort)]
    (println s)))

;;; main

(def options
  [["-h" "--help" "Print help"]
   [nil "--version" "Print version"]])

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
