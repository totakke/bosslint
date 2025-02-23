(ns bosslint.main
  (:require [bosslint.config :as config]
            [bosslint.git :as git]
            [bosslint.linter :as linter]
            (bosslint.linter actionlint
                             checkstyle
                             clj-kondo
                             cljfmt
                             cljstyle
                             dartanalyzer
                             dotenv-linter
                             eastwood
                             flake8
                             hadolint
                             jsonlint
                             kubeval
                             markdownlint
                             shellcheck
                             sql-lint
                             stylelint
                             swiftlint
                             tflint
                             yamllint)
            [bosslint.process :as process]
            [clj-sub-command.core :as cmd]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [io.aviso.ansi :as ansi])
  (:gen-class))

(def version
  (if-let [url (io/resource "VERSION")]
    (slurp url)
    "?.?.?"))

(defn- list-linters []
  (sort-by name (descendants :bosslint/linter)))

(def ^:private path-type-pairs
  {#"\.clj$" :clj
   #"\.cljc$" :cljc
   #"\.cljs$" :cljs
   #"\.css$" :css
   #"\.dart$" :dart
   #"Dockerfile(\.[-\w]+)?$" :docker
   #"(^|/)\.env$" :dot-env
   #"\.java$" :java
   #"\.json$" :json
   #"\.(md|markdown)$" :markdown
   #"\.py$" :python
   #"\.s[ac]ss$" :sass
   #"\.sh$" :shell
   #"\.sql$" :sql
   #"\.swift$" :swift
   #"\.tf$" :terraform
   #"^\.github/workflows/.+\.ya?ml$" :workflow
   #"\.ya?ml$" :yaml})

(defn path->types [s]
  (let [types (->> path-type-pairs
                   (filter #(re-find (first %) s))
                   (map second))]
    (if (seq types)
      (set types)
      #{:other})))

(defn enum-files [ref1 ref2]
  (let [top-dir (git/top-dir)]
    (->> (if (= ref1 ":all")
           (git/ls-files)
           (git/diff ref1 ref2))
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
  (binding [linter/*verbose?* (:verbose options)
            process/*working-directory* (:directory options)]
    (let [diff-files (enum-files ref1 ref2)
          file-group (->> diff-files
                          (mapcat (fn [{:keys [git-path] :as m}]
                                    (map #(assoc m :type %) (path->types git-path))))
                          (group-by :type))
          conf (if (:config options)
                 (config/load-config (:config options))
                 (config/load-config))
          enabled-linter? (if (:linter options)
                            (comp (set (:linter options)) name)
                            #(not (:disabled? (get conf (keyword (name %))))))
          linters (filter enabled-linter? (list-linters))
          statuses (atom #{})]
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
              (let [status (linter/lint key files (get conf (keyword (name key))))]
                (swap! statuses conj status)
                (case status
                  :success (println (ansi/cyan "==>") "Success ✅")
                  :warning (println (ansi/cyan "==>") "Warning ⚠️")
                  :error (println (ansi/cyan "==>") "Error ❌")
                  nil))
              (newline))
          (when linter/*verbose?*
            (println (str (ansi/green (linter/name key)) ":"))
            (println (ansi/cyan "No files"))
            (newline))))

      (condp #(get %2 %1) @statuses
        :error (throw (ex-info nil {:status 1}))
        :warning (throw (ex-info nil {:status 2}))
        nil))))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (when msg (println msg))
  (System/exit status))

;; check

(def check-cmd-options
  [["-c" "--config CONFIG" "Specify a configuration file"]
   ["-C" "--directory DIR" "Specify an alternate working directory"
    :default "."]
   ["-l" "--linter LINTER" "Select linter"
    :assoc-fn (let [name-map (into {} (map (juxt linter/name name)
                                           (list-linters)))]
                (fn [m k v]
                  (update m k #(conj (or % []) (name-map v v)))))
    :validate [(set (mapcat (juxt name linter/name) (list-linters)))
               "Unsupported linter"]]
   ["-v" "--verbose" "Make bosslint verbose during the operation"]
   ["-h" "--help" "Print this help and exit"]])

(defn check-cmd-usage [options-summary]
  (->> ["Usage: bosslint check [<options>] [<commit> [<commit>]]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn validate-check-cmd-args [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args check-cmd-options)]
    (cond
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

;; linters

(defn linters-cmd [_]
  (doseq [k (list-linters)]
    (println (str (linter/name k) " (" (name k) ")"))))

;; main

(def options
  [["-h" "--help" "Print this help and exit"]
   [nil "--version" "Print the version and exit"]])

(def commands
  [["check" "Check files"]
   ["linters" "Show linters"]])

(defn usage [options-summary commands-summary]
  (->> [(str "bosslint v" version)
        ""
        "Usage: bosslint [--help] [--version] <command> [<args>]"
        ""
        "Options:"
        options-summary
        ""
        "Commands:"
        commands-summary
        ""
        "For more info, see https://github.com/totakke/bosslint#usage"]
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
          (exit (:status (ex-data e) 1) (ex-message e)))))))
