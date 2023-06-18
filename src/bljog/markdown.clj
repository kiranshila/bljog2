(ns bljog.markdown
  (:require
   [clojure.string :as string]
   [clojure.java.io :as io]
   [cybermonday.utils :refer [gen-id make-hiccup-node]]
   [cybermonday.core :as cm])
  (:import
   [java.io File]))

(set! *warn-on-reflection* true)

(def default-markdown-data
  {:tags {} ;; Map from tag to vec of post idxs
   :posts [] ;; Vec of {:body :frontmatter :href}
   :hrefs {}}) ;; Map of post link path (filename) to post idx

(defonce markdown-data (atom default-markdown-data))

(defn parse-math [[_ _ & [math]]]
  (str "\\(" math "\\)"))

(defn lower-fenced-code-block [[_ {:keys [language]} code]]
  (if (or (= language "math")
          (= language "latex")
          (= language "tex"))
    (str "$$" code "$$")
    [:pre [:code {:class (str "language-" language)} code]]))

(defn lower-heading [[_ attrs & body :as node]]
  (make-hiccup-node
   (keyword (str "h" (:level attrs)))
   (dissoc
    (let [id (if (nil? (:id attrs))
               (gen-id node)
               (:id attrs))]
      (assoc attrs
             :id id
             :class "anchor"
             :href (str "#" id)))
    :level)
   body))

(defn parse-md [rdr href]
  (assoc
   (cm/parse-md
    rdr
    {:lower-fns
     {:markdown/inline-math parse-math
      :markdown/heading lower-heading
      :markdown/fenced-code-block lower-fenced-code-block}})
   :href href))

(defn md-files [path]
  (for [^File file (.listFiles (io/file path))
        :when (not (.isDirectory file))
        :let [file-rdr (io/reader file)
              fname (.getName file)]
        :when (string/ends-with? fname "md")
        :let [dot-idx (string/last-index-of fname ".")
              href (subs fname 0 dot-idx)]]
    (parse-md file-rdr href)))

(defn process-post-markdown [post-path]
  (reset! markdown-data default-markdown-data)
  (let [posts (->> (md-files post-path)
                   (sort-by #(get-in % [:frontmatter :date]) #(compare %2 %1)))]
    (doseq [[idx post] (map-indexed (fn [idx post] [idx post]) posts)]
      ;; Add the post body and frontmatter to the post vec
      (swap! markdown-data update :posts conj post)
      ;; Assoc the href
      (swap! markdown-data update-in [:posts idx] assoc :href (:href post))
      (swap! markdown-data update :hrefs assoc (:href post) idx)
      (when-let [ts (:tags (:frontmatter post))]
        (doseq [tag ts]
          (swap! markdown-data update-in [:tags tag] conj idx))))))

(defonce page-markdown
  (->> (md-files "./resources/pages/")
       (map (fn [{:keys [href] :as page}] [href page]))
       (into {})))
