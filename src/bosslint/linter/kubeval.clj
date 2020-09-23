(ns bosslint.linter.kubeval
  (:require [bosslint.linter :as linter :refer [deflinter]]
            [clj-yaml.core :as yaml]
            [clojure.java.shell :as shell]
            [clojure.string :as string]))

;; kubectl api-resources
(def ^:private kube-kinds
  #{"Binding"
    "ComponentStatus"
    "ConfigMap"
    "Endpoints"
    "Event"
    "LimitRange"
    "Namespace"
    "Node"
    "PersistentVolumeClaim"
    "PersistentVolume"
    "Pod"
    "PodTemplate"
    "ReplicationController"
    "ResourceQuota"
    "Secret"
    "ServiceAccount"
    "Service"
    "MutatingWebhookConfiguration"
    "ValidatingWebhookConfiguration"
    "CustomResourceDefinition"
    "APIService"
    "ControllerRevision"
    "DaemonSet"
    "Deployment"
    "ReplicaSet"
    "StatefulSet"
    "TokenReview"
    "LocalSubjectAccessReview"
    "SelfSubjectAccessReview"
    "SelfSubjectRulesReview"
    "SubjectAccessReview"
    "HorizontalPodAutoscaler"
    "CronJob"
    "Job"
    "CertificateSigningRequest"
    "Lease"
    "ENIConfig"
    "EndpointSlice"
    "Ingress"
    "NetworkPolicy"
    "RuntimeClass"
    "PodDisruptionBudget"
    "PodSecurityPolicy"
    "ClusterRoleBinding"
    "ClusterRole"
    "RoleBinding"
    "Role"
    "PriorityClass"
    "CSIDriver"
    "CSINode"
    "StorageClass"
    "olumeAttachment"})

(defn- kube-yaml?
  [{:keys [absolute-path]}]
  (->> (string/split (slurp absolute-path) #"---\n")
       (map yaml/parse-string)
       (map :kind)
       (some kube-kinds)
       boolean))

(deflinter :linter/kubeval
  (name [] "kubeval")

  (files [file-group]
    (->> (linter/select-files file-group [:yaml])
         (filter kube-yaml?)))

  (lint [files conf]
    (when (linter/check-command "kubeval")
      (let [args (concat ["kubeval"]
                         (:command-options conf)
                         (map :absolute-path files))
            ret (apply shell/sh args)]
        (println (string/trim-newline (:out ret)))))))
