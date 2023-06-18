(ns bljog.core
  (:require
   [bljog.markdown :refer [process-post-markdown process-page-markdown]]
   [bljog.server :refer [start-server]])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main [& [post-path]]
  (process-post-markdown post-path)
  (process-page-markdown)
  (start-server post-path))
