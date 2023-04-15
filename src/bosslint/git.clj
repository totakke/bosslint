(ns bosslint.git
  (:require [clj-jgit.porcelain :as jgit]
            [clojure.java.io :as io])
  (:import (java.io File FileNotFoundException)
           (org.eclipse.jgit.api DiffCommand Git)
           (org.eclipse.jgit.diff DiffEntry DiffEntry$ChangeType)
           org.eclipse.jgit.revwalk.RevCommit
           (org.eclipse.jgit.treewalk CanonicalTreeParser FileTreeIterator
                                      TreeWalk)))

(defn- load-repo*
  [^File dir]
  (try
    (jgit/load-repo dir)
    (catch FileNotFoundException _
      (load-repo* (.getParentFile dir)))))

(defn load-repo
  ^Git [path]
  (load-repo* (io/file path)))

(defn- parse-ref
  [^Git repo ref]
  (let [repository (.getRepository repo)]
    (if (empty? ref)
      (FileTreeIterator. repository)
      (let [tree (CanonicalTreeParser.)]
        (.reset tree (.newObjectReader repository)
                (.resolve repository (str ref "^{tree}")))
        tree))))

(defn diff
  [^Git repo old-ref new-ref]
  (as-> (.diff repo) ^DiffCommand cmd
      (.setOldTree cmd (parse-ref repo old-ref))
      (.setNewTree cmd (parse-ref repo new-ref))
      (.call cmd)
      (filter #(#{DiffEntry$ChangeType/ADD
                  DiffEntry$ChangeType/MODIFY
                  DiffEntry$ChangeType/RENAME}
                (.getChangeType ^DiffEntry %)) cmd)
      (mapv #(.getNewPath ^DiffEntry %) cmd)))

(defn ls-files
  [^Git repo]
  (let [treewalk (TreeWalk. (.getRepository repo))
        id (.getTree ^RevCommit (:id (first (jgit/git-log repo))))
        files (transient [])]
    (doto treewalk
      (.setRecursive true)
      (.reset id))
    (while (.next treewalk)
      (conj! files (.getPathString treewalk)))
    (persistent! files)))

(defn top-dir
  [^Git repo]
  (.. repo getRepository getWorkTree getAbsolutePath))
