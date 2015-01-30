(ns leiningen.packer
  (:require [leiningen.help :as h]
            [leiningen.core.main :as m]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
  (:import java.io.File))

(set! *warn-on-reflection* true)

(defn exists? [file]
  (let [f (io/file file)]
    (when (.exists f)
      f)))

(defn mkdir [dir]
  (let [d (io/file dir)]
    (when-not (and (.exists d)
                   (.isDirectory d))
      (let [p (.getParent ^java.io.File d)]
        (mkdir p))
      (.mkdir d))
    d))

(defn make-path [root leaf]
  (io/file (str (.getPath ^java.io.File root)
                (File/separator)
                (io/file (.getName ^java.io.File leaf)))))

(defn directory? [file]
  (.isDirectory (io/file file)))

(defn re-files [re]
  (let [f (io/file (str re))
        d (io/file (.getParent f))]
    (filter #(re-find re (.getPath ^java.io.File %)) (file-seq d))))

(defn copy-file [file dir]
  (println "@file:" file "|dir:" dir)
  (let [n (.getName ^java.io.File (io/file file))
        p (str (.getPath ^java.io.File dir) (File/separator) n)]
    (io/copy file (io/file p))))

(defn copy-dir-leaf [source destination]
  (let [s (io/file source)
        d (io/file destination)
        l (.getName s)
        r (.getName d)]
    (when-not (= l r)
      (mkdir (make-path d s)))))

(defn copy-dir [source destination]
  (let [s (io/file source)
        d (mkdir destination)]
    (println "#dd:" d)
    (doseq [f (.listFiles ^java.io.File s)]
      (if-not (directory? f)
        (copy-file f d)
        (do
          (println "!f:" f "|mp:" (make-path d f) "|d:" d)
          (copy-dir f (make-path d f)))))))

(defn transfer [mapping]
  (let [s (seq mapping)]
    (pprint (str "#mapping:" mapping))
    (doseq [{:keys [source-paths target-path]} s]
      (println (str "%s:" source-paths "|d:" target-path))
      (let [d (mkdir target-path)]
        (doseq [p source-paths]
          (if (instance? java.util.regex.Pattern p)
            (doseq [f (re-files p)] (copy-file f d))
            (if (directory? p)
              (do (copy-dir-lear p d)
                  (copy-dir p d))
              (copy-file p d))))))))

(defn- once
  [project
   {{:keys [mapping target]
     :as options} :pack}
   args]
  (transfer mapping))

(defn- clean
  [project
   options
   args]
  (println "##in clean")
  (pprint options)
  (pprint args))

(defn packer
  "Run the packer plugin."
  {:help-arglists '([once clean])
   :subtasks [#'once #'clean]}
  ([project]
   (println (h/help-for "packer"))
   (m/abort))
  ([project subtask & args]
   (let [options (select-keys project [:pack])]
     (case subtask
       "once" (once project options args)
       "clean" (clean project options args)
       (do
         (println "Subtask"
                  (str " subtask " "not found."
                       (h/subtask-help-for *ns* #'packer)))
         (m/abort))))))
