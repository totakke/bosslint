(ns bosslint.main
  (:require [bosslint.linters :as linters]
            [bosslint.util :as util]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def linters
  [::linters/cljfmt
   ::linters/clj-kondo
   ::linters/eastwood
   ::linters/stylelint])

(def excludes
  [#"project.clj$"])

(defn git-diff [ref]
  (util/check-command "git")
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

(defn run [ref]
  (let [files (if ref
                (group-by path->type (git-diff ref))
                :all)]
    (println "Files:" files)
    (doseq [l linters]
      (linters/lint l files))))

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
  (let [{:keys [ref exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (try
        (run ref)
        (System/exit 0)
        (catch Exception e
          (exit 1 (.getMessage e)))))))
