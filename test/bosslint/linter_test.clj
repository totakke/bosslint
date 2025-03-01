(ns bosslint.linter-test
  (:require [bosslint.linter :as linter]
            [clojure.test :refer [are deftest]]))

(deftest path->types-test
  (are [s k] (= (linter/path->types s) k)
    "path/to/foo.clj"           #{:clj}
    "path/to/foo.cljc"          #{:cljc}
    "path/to/foo.cljs"          #{:cljs}
    "path/to/foo.css"           #{:css}
    "path/to/foo.dart"          #{:dart}

    "Dockerfile"                #{:docker}
    "path/to/Dockerfile"        #{:docker}
    "path/to/Dockerfile.dev"    #{:docker}
    "foo.Dockerfile"            #{:other}
    "path/to/foo.Dockerfile"    #{:other}

    ".env"                      #{:dot-env}
    "path/to/.env"              #{:dot-env}
    "foo.env"                   #{:other}
    "path/to/foo.env"           #{:other}

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
