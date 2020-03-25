(ns bosslint.linter.eastwood-test
  (:require [bosslint.linter.eastwood :as e]
            [clojure.test :refer [are deftest]]))

(deftest path->ns-test
  (are [path ns] (= (#'e/path->ns path) ns)
    "src/foo/bar/foobar.clj"                    "foo.bar.foobar"
    "src/foo/bar/foobar.cljc"                   "foo.bar.foobar"
    "test/foo/bar/foobar_test.clj"              "foo.bar.foobar-test"
    "src/foo/foo_bar/foobar.clj"                "foo.foo-bar.foobar"
    "src/clj/foo/bar/foobar.clj"                "foo.bar.foobar"
    "src/cljc/foo/bar/foobar.cljc"              "foo.bar.foobar"
    "dev/clj/dev.clj"                           "dev"
    "src/main/clojure/foo/bar/foobar.clj"       "foo.bar.foobar"
    "test/main/clojure/foo/bar/foobar_test.clj" "foo.bar.foobar-test")
  (are [path] (nil? (#'e/path->ns path))
    "src/foo/bar/foobar.cljs"
    "src/cljs/foo/bar/foobar.cljs"))
