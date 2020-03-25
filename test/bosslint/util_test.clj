(ns bosslint.util-test
  (:require [bosslint.util :as u]
            [clojure.test :refer [deftest is]]))

(deftest command-exists?-test
  (is (true? (u/command-exists? "ls")))
  (is (false? (u/command-exists? "notfound"))))
