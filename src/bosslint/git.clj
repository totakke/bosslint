(ns bosslint.git
  (:require [bosslint.process :as process]
            [clojure.java.process :as jprocess]
            [clojure.string :as string]))

(defn- assert-command
  [command]
  (when-not (process/command-exists? command)
    (throw (ex-info (str "Command not found: " command) {}))))

(defn diff
  [ref1 ref2]
  (assert-command "git")
  (let [args (cond-> ["git" "diff" "--name-only" "--diff-filter=AMRTU"]
               ref1 (conj ref1)
               ref2 (conj ref2))
        proc (apply process/start {:out :pipe, :err :pipe} args)
        exit @(jprocess/exit-ref proc)]
    (if (zero? exit)
      (string/split-lines (slurp (jprocess/stdout proc)))
      (throw (ex-info (slurp (jprocess/stderr proc)) {:status exit})))))

(defn ls-files
  []
  (assert-command "git")
  (->> (process/exec "git" "ls-files" "--full-name")
       string/split-lines))

(defn top-dir
  []
  (assert-command "git")
  (-> (process/exec "git" "rev-parse" "--show-toplevel")
      string/trim-newline))
