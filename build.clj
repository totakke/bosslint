(ns build
  (:require [clojure.tools.build.api :as b]))

(def version (format "0.7.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/bosslint.jar")
(def bin-file "target/bosslint")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/write-file {:path (str class-dir "/VERSION")
                 :string version})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:elide-meta [:added :doc :file :line]
                                 :direct-linking true}
                  :java-opts ["-Dio.aviso.ansi.enable=true"]})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'bosslint.main}))

(defn native-image [_]
  (let [native-image-bin (str (System/getenv "GRAALVM_HOME")
                              "/bin/native-image")]
    (b/process {:command-args [native-image-bin "-jar" uber-file bin-file
                               "--diagnostics-mode"
                               "--initialize-at-build-time"
                               "--no-fallback"]})))

(defn bin [_]
  (clean nil)
  (uber nil)
  (native-image nil))
