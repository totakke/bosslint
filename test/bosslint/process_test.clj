(ns bosslint.process-test
  (:require [bosslint.process :as process]
            [clojure.test :refer [deftest is]]))

(deftest command-exists?-test
  (is (true? (process/command-exists? "ls")))
  (is (false? (process/command-exists? "notfound"))))
