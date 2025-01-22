(ns bosslint.main-test
  (:require [bosslint.main :as b]
            [clojure.test :refer [are deftest]]))

(deftest path->types-test
  (are [s k] (= (b/path->types s) k)
    "path/to/foo.clj"           #{:clj}
    "path/to/foo.cljc"          #{:cljc}
    "path/to/foo.cljs"          #{:cljs}
    "path/to/foo.dart"          #{:dart}
    "path/to/Dockerfile"        #{:docker}
    "path/to/Dockerfile.dev"    #{:docker}
    "path/to/foo.java"          #{:java}
    "path/to/README.md"         #{:markdown}
    "path/to/foo.py"            #{:python}
    "path/to/foo.sass"          #{:sass}
    "path/to/foo.scss"          #{:sass}
    "path/to/foo.sh"            #{:shell}
    "path/to/foo.sql"           #{:sql}
    "path/to/foo.swift"         #{:swift}
    "path/to/foo.yml"           #{:yaml}
    "path/to/foo.yaml"          #{:yaml}
    ".github/workflows/foo.yml" #{:yaml :workflow}
    "path/to/foo"               #{:other}))
