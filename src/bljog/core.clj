(ns bljog.core
  (:require
   [bljog.markdown :refer [process-post-markdown]]
   [bljog.server :refer [start-server]])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main [& [post-path]]
  (process-post-markdown post-path)
  (start-server post-path))
