(ns leiningen.packer
  (:require [leiningen.help :as h]
            [leiningen.core.main :as m]
            [me.raynes.fs :as fs]
            [clojure.pprint :refer [pprint]]))

(set! *warn-on-reflection* true)

(defn- find-files [re-dir]
  (map str (fs/find-files (str (fs/parent (fs/file (str re-dir))))
                          (re-pattern (apply str (fs/split-ext (str re-dir)))))))

(defn- do-mapping [mapping]
  (let [s (seq mapping)]
    (doseq [{:keys [source-paths target-path]} s]
      (when-not (fs/exists? target-path)
        (fs/mkdir target-path))
      (doseq [sp source-paths]
        (if (instance? java.util.regex.Pattern sp)
          (doseq [p (find-files sp)]
            (fs/copy-dir p (str (fs/file target-path))))
          (fs/copy-dir sp (str (fs/file target-path))))))))

(defn- once
  [project
   {{:keys [mapping target]
     :as options} :pack}
   args]
  (println "##in once")
  (pprint options)
  (println "====")
  (pprint mapping)
  (println "====")
  (pprint target)
  (println "====")
  (do-mapping mapping))

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
