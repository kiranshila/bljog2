(ns bljog.server
  (:require
   [bljog.views :as v]
   [muuntaja.core :as m]
   [reitit.ring :as ring]
   [ring.logger :as logger]
   [reitit.ring.coercion :as rrc]
   [reitit.coercion.spec]
   [clojure.tools.logging :as log]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.ring.middleware.exception :as exception]
   [org.httpkit.server :as hks]))

(set! *warn-on-reflection* true)

(defn render-root [component]
  (-> component
      v/root
      v/render
      (update :body #(str "<!DOCTYPE html>" %))))

(defn router [asset-path]
  (logger/wrap-with-logger
   (ring/ring-handler
    (ring/router
     [["/"      {:get (fn [_] (render-root (v/home)))}]
      ["/blog" {:get (fn [_] (render-root (v/blog)))}]
      ["/post/{title}" {:get (fn [route] (render-root (v/post (get-in route [:path-params :title]))))}]
      ["/tag/{tag}" {:get (fn [route] (render-root (v/tag (get-in route [:path-params :tag]))))}]
      ["/open-source" {:get (fn [_] (render-root (v/open-source)))}]
      ["/publications" {:get (fn [_] (render-root (v/publications)))}]
      ["/hire" {:get (fn [_] (render-root (v/hire)))}]
      ["/about" {:get (fn [_] (render-root (v/about)))}]]
     {:conflicts (constantly nil)
      :data {:coercion   reitit.coercion.spec/coercion
             :muuntaja   m/instance
             :middleware [parameters/parameters-middleware
                          rrc/coerce-request-middleware
                          muuntaja/format-response-middleware
                          exception/exception-middleware
                          rrc/coerce-response-middleware]}})
    (ring/routes
     (ring/create-file-handler {:path "/" :root "./resources/public"})
     (ring/create-file-handler {:path "/" :root asset-path})
     (ring/create-default-handler)))))

(defonce server (atom nil))

(defn start-server [asset-path]
  (reset!
   server
   (hks/run-server
    (router asset-path)
    {#_#_:worker-pool (java.util.concurrent.Executors/newVirtualThreadPerTaskExecutor) ;; Enable with Java19
     :port 8080
     :legacy-return-value? false}))
  (log/info "Server started"))

(defn stop-server []
  (when-not (nil? @server)
    (swap! server hks/server-stop!)))
