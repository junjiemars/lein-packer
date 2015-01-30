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

(defn copy-file [file dir]
  "Copy the file to the dir"
  (let [f (io/file file)
        d (io/file dir)]
    (m/debug  "#copy-file:" file " to " dir)
    (io/copy file (join-path d f))))

(defn copy-dir [source destination]
  "Iterated copy directories and it's sub directories"
  (let [s (io/file source)
        d (mkdir destination)]
    (doseq [f (.listFiles ^java.io.File s)]
      (if-not (directory? f)
        (copy-file f d)
        (copy-dir f (join-path d f))))))

(defn transfer [mapping]
  "Transfer the mapping files"
  (let [s (seq mapping)]
    (doseq [{:keys [source-paths target-path]} s]
      (let [d (mkdir target-path)]
        (doseq [p source-paths]
          (if (instance? java.util.regex.Pattern p)
            (doseq [f (re-files p)] (copy-file f d))
            (if (directory? p)
              (copy-dir p d)
              (copy-file p d))))))))

(defn once
  [project
   {{:keys [mapping target]
     :as options} :pack}
   args]
  (do
    (m/debug "#once:")
    (m/debug "#mapping:" mapping)
    (transfer mapping)))

(defn clean
  [project
   options
   args]
  (do
    (m/debug "#clean:")
    (m/debug "#options:" options)
    (m/debug "#args:" args)))

(defn packer
  "Run the packer plugin."
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
