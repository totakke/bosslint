(ns bosslint.linter.ansible-lint
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [bosslint.process :as process]))

(defn- match-any?
  [patterns ^String s]
  (boolean (some #(re-find (re-pattern %) s) patterns)))

(deflinter :linter/ansible-lint
  (name [] "ansible-lint")

  (files [file-group conf]
    (let [ansible-paths (set (map :git-path (get file-group :ansible)))
          includes (:include-paths conf)
          excludes (:exclude-paths conf)]
      (->> (linter/select-files file-group [:yaml])
           (filter (fn [{:keys [git-path]}]
                     (or (contains? ansible-paths git-path)
                         (and (seq includes) (match-any? includes git-path)))))
           (remove (fn [{:keys [git-path]}]
                     (and (seq excludes) (match-any? excludes git-path)))))))

  (lint [{:keys [files]} conf]
    (when (linter/check-command "ansible-lint")
      (let [args (concat ["ansible-lint"]
                         (:command-options conf)
                         (map :absolute-path files))]
        (if (zero? (apply process/run args))
          :success
          :error)))))
