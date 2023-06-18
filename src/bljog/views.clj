(ns bljog.views
  (:require
   [rum.core :refer [defc render-static-markup]]
   [bljog.markdown :as md]
   [tick.core :as t]
   [tick.locale-en-us]))

(set! *warn-on-reflection* true)

(defc root
  "A component to draw other components into, providing the top-level HTML stuff"
  [body]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    ;; Font Loading
    [:script {:src "/fonts.js"}]
    [:script {:src "//cdn.jsdelivr.net/npm/webfontloader@1.6.28/webfontloader.js"
              :crossorigin "anonymous"}]
    ;; KaTeX Resources
    [:link {:rel "stylesheet"
            :href "//cdn.jsdelivr.net/npm/katex@0.16.7/dist/katex.min.css"
            :crossorigin "anonymous"}]
    [:script {:src "//cdn.jsdelivr.net/npm/katex@0.16.7/dist/katex.min.js"
              :crossorigin "anonymous"}]
    ;; Highlight JS resources
    [:link {:rel "stylesheet"
            :href "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/styles/base16/classic-dark.min.css"
            :crossorigin "anonymous"}]
    ;; Automatically render math
    [:script {:defer true
              :src "//cdn.jsdelivr.net/npm/katex@0.16.7/dist/contrib/auto-render.min.js"
              :crossorigin "anonymous"
              :onload "renderMathInElement(document.body);"}]
    [:link {:rel "stylesheet" :href "/out.css"}]
    ;; Highlight all the code with web workers
    [:script  {:defer true
               :src "/highlight.js"
               :onload "highlight_code();"}]
    ;; Mobile navbar toggle
    [:script {:defer true
              :src "/nav.js"}]]
   [:body {:class "bg-slate-800 text-white"}
    body]])

(defc site-logo [page small?]
  [:div
   [:span {:class "text-white"} "("]
   [:a {:class "text-pink-300 hover:text-pink-400" :href "/"} (if small? "lmc" "logic-memory-center")]
   [:span {:class "text-cyan-200"} (str " :" page)]
   [:span {:class "text-white"} ")"]])

(defc nav [page]
  [:header {:class "h-16 bg-slate-800 sticky top-0"}
   [:div#site-link {:class "absolute left-2 top-4 h-12 w-30 text-xl"}
    [:h1 {:class "hidden md:block"} (site-logo page false)]
    [:h1 {:class "md:hidden"} (site-logo page true)]]
   [:div#nav-container {:class "bg-slate-800 p-2 flex flex-col items-end"}
    [:div#nav-icon {:class "h-12 w-12 p-2 group md:hidden"}
     [:svg {:xmlns "<http://www.w3.org/2000/svg>"
            :view-box "0 0 20 20"
            :fill "white"
            :class "h-full w-full group-hover:fill-slate-500"}
      [:path {:fill-rule "evenodd"
              :d "M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z"
              :clip-rule "evenodd"}]]]
    [:ul#nav-menu {:class "hidden w-full space-y-2 pr-3 font-semibold text-xl text-right text-white md:h-12 md:flex md:flex-row md:items-center md:justify-end md:space-x-5 md:space-y-0"}
     [:li {:class "hover:text-slate-300"} [:a {:href "/blog"} "blog"]]
     [:li {:class "hover:text-slate-300"} [:a {:href "/open-source"} "open-source"]]
     [:li {:class "hover:text-slate-300"} [:a {:href "/publications"} "publications"]]
     [:li {:class "hover:text-slate-300"} [:a {:href "/hire"} "hire"]]
     [:li {:class "hover:text-slate-300"} [:a {:href "/about"} "about"]]]]])

(defc post-list
  "Display a vec of posts as clickable links"
  [posts]
  [:table {:class "table-auto mx-4 mt-8 md:mx-auto"}
   [:tbody
    (for [post posts
          :let [{:keys [frontmatter href]} post]]
      [:tr
       [:td
        [:div {:class "text-xs mr-3"}
         (t/format (t/formatter "MMM dd, yy") (t/date (:date frontmatter)))]]
       [:td {:class "py-4"}
        [:div {:class "max-w-prose block text-xl"}
         [:a {:href (str "/post/" href)} (:title frontmatter)]
         [:br]
         [:div {:class "text-xs"}
          [:strong "Tags:"]
          (for [tag (:tags frontmatter)] [:a {:class "ml-1" :href (str "/tag/" tag)} tag])]]]])]])

(defc home []
  [:div
   (nav "home")
   [:div
    [:h1 {:class "text-2xl mx-8 text-center mb-8"} "Recent Posts"]
    (post-list (->> (:posts @md/markdown-data)
                    (filter #(not (:draft (:frontmatter %))))
                    (take 7)))]])

(defc blog
  "One continuous list of all the blogposts, sorted by time"
  []
  [:div
   (nav "blog")
   (post-list (->> (:posts @md/markdown-data)
                   (filter #(not (:draft (:frontmatter %))))))])

(defc markdown-content
  "Render content either for a page or a post"
  [post nav-name]
  [:div
   (nav nav-name)
   [:div {:class "w-full px-6 py-12 md:max-w-3xl md:mx-auto lg:max-w-4xl lg:pt-16 lg:pb-28"}
    [:article {:class "prose prose-invert prose-img:mx-auto"}
     [:div
      (when-let [title (get-in post [:frontmatter :title])]
        [:h1 {:class "text-4xl text-center"} title])
      (:body post)]]]])

(defc post
  "The view that renders a blog post"
  [post-href]
  (let [post-idx ((@md/markdown-data :hrefs) post-href)
        post (nth (:posts @md/markdown-data) post-idx)]
    (markdown-content post "post")))

(defc tag
  "Show all blog posts that share the tag"
  [tag]
  [:div
   (nav "tag")
   [:h1 {:class "text-2xl mx-8 text-center mb-8"} "Posts Tagged: " tag]
   (post-list (->> ((:tags @md/markdown-data) tag)
                   (map #(nth (:posts @md/markdown-data) %))
                   (filter #(not (:draft (:frontmatter %))))))])

(defc about []
  (markdown-content
   (@md/page-markdown "about")
   "about"))

(defc hire []
  (markdown-content
   (@md/page-markdown "hire_me")
   "hire"))

(defc open-source []
  (markdown-content
   (@md/page-markdown "open_source")
   "open-source"))

(defc publications []
  (markdown-content
   (@md/page-markdown "publications")
   "publications"))

(defn render [component]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (render-static-markup component)})
