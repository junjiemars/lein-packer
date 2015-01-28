(ns leiningen.packer
  (:require [leiningen.help :as h]
            [leiningen.core.main :as m]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]))

(defn- once
  [project
   options
   args]
  (println "#in once"))

(defn- clean
  [project
   options
   args]
  (println "#in clean"))

(defn packer
  "Run the packer plugin."
  {:help-arglists '([once clean])
   :subtasks [#'once #'clean]}
  ([project]
   (println (h/help-for "packer"))
   (m/abort))
  ([project subtask & args]
   (let [options (select-keys project [:chrome :safari])]
     (case subtask
       "once" (once project options args)
       "clean" (clean project options args)
       (do
         (println "Subtask"
                  (str " subtask " "not found."
                       (h/subtask-help-for *ns* #'packer)))
         (m/abort))))))
