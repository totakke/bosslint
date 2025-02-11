(ns build
  (:require [clojure.java.process :as p]
            [clojure.tools.build.api :as b]))

(def version (format "0.6.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/bosslint.jar")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/write-file {:path (str class-dir "/VERSION")
                 :string version})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'bosslint.main}))

(defn native-image [_]
  (uber nil)
  (let [native-image-bin (str (System/getenv "GRAALVM_HOME")
                              "/bin/native-image")]
    (p/exec {:out :inherit, :err :inherit}
            native-image-bin "-jar" uber-file
            "--diagnostics-mode"
            "--initialize-at-build-time"
            "--no-fallback")))
