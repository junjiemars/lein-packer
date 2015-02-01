(ns leiningen.packer
  (:require [leiningen.help :as h]
            [leiningen.core.main :as m]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
  (:import java.io.File))

(set! *warn-on-reflection* true)

(defn exists? [file]
  "If file is exists then return it, otherwise return nil"
  (let [f (io/file file)]
    (when (.exists f)
      f)))

(defn mkdir [dir]
  "If dir is not exists or is not a directory then return it, "
  "otherwise return it directly"
  (let [d (io/file dir)]
    (when-not (and (.exists d)
                   (.isDirectory d))
      (let [p (.getParent ^java.io.File d)]
        (mkdir p)
        (.mkdir d)))
    d))

(defn join-path [root leaf]
  "Join root path with leaf path and return it"
  (io/file (str (.getPath ^java.io.File root)
                (File/separator)
                (io/file (.getName ^java.io.File leaf)))))

(defn directory? [file]
  "Is the file is directory?"
  (.isDirectory (io/file file)))

(defn re-files [re]
  "Return the file seq designated by re pattern"
  (let [f (io/file (str re))
        d (io/file (.getParent f))]
    (filter #(re-find re (.getPath ^java.io.File %)) (file-seq d))))

(defn re-excludes [re file]
  "If the file is not include in re pattern then return it"
  (let [f (io/file file)
        r (seq re)]
    (when (not-any? #(not (nil? %))
                    (map #(re-find % (.getName f)) r))
      f)))

(defn copy-file [file dir excludes]
  "Copy the file to the dir"
  (let [f (io/file file)
        d (io/file dir)]
    (when (re-excludes excludes file)
      (m/debug  "#copy-file:" f "|" d)
      (io/copy f (join-path d f)))))

(defn copy-dir [source destination excludes]
  "Iterated copy directories and it's sub directories"
  (let [s (io/file source)
        d (mkdir destination)]
    (doseq [f (.listFiles ^java.io.File s)]
      (if-not (directory? f)
        (copy-file f d excludes)
        (copy-dir f (join-path d f) excludes)))))

(defn delete-dir [dir]
  "Remove dir recursively"
  (let [d (io/file dir)]
    (m/debug "#delete-dir:" d)
    (if (directory? d)
      (do 
        (doseq [f (.listFiles d)]
          (delete-dir f))
        (io/delete-file d))
      (io/delete-file d))))

(defn transfer-mapping [mapping]
  "Transfer the mapping files"
  (let [s (seq mapping)]
    (doseq [{:keys [source-paths target-path excludes]} s]
      (let [d (mkdir target-path)]
        (doseq [p source-paths]
          (if (instance? java.util.regex.Pattern p)
            (doseq [f (re-files p)] (copy-file f d excludes))
            (if (directory? p)
              (copy-dir p d excludes)
              (copy-file p d excludes))))))))

(defn remove-target [mapping target]
  "Remove the mapping files and target file"
  (let [s (seq mapping)
        ;m target
        ]
    (doseq [{:keys [source-paths target-path]} s]
      (delete-dir target-path))))

(defn once
  "Pack the resources via mapping files, directories, regex-patterns"
  [project
   {{:keys [mapping target]
     :as options} :pack}
   args]
  (m/debug "#once:")
  (m/debug "#mapping:" mapping)
  (transfer-mapping mapping))

(defn clean
  "Clean the packed resources"
  [project
   {{:keys [mapping target]
     :as options} :pack} 
   args]
  (m/debug "#clean:")
  (m/debug "#options:" options)
  (m/debug "#args:" args)
  (remove-target mapping target))

(defn packer
  "Pack the file resources of the project."
  {:help-arglists '([once clean])
   :subtasks [#'once #'clean]}
  ([project]
   (do
     (m/info (h/help-for "packer"))
     (m/abort)))
  ([project subtask & args]
   (let [options (select-keys project [:pack])]
     (case subtask
       "once" (once project options args)
       "clean" (clean project options args)
       (do
         (m/info "Subtask "  subtask " not found."
                 (h/subtask-help-for *ns* #'packer))
         (m/abort))))))
