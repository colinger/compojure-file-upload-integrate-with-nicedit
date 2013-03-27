(ns fileupload.core
    (:use [net.cgrand.enlive-html
              :only [deftemplate defsnippet content clone-for
                     nth-of-type first-child do-> set-attr sniptest at emit*]]
          [compojure.core]
		  [ring.middleware.params]
		  [ring.middleware.multipart-params]
          [ring.adapter.jetty]
		  [hiccup.core]
          [http.util.context])
    (:require (compojure [route :as route])
            (ring.util [response :as response])
            (clojure.contrib [duck-streams :as ds])
            (clojure.contrib [string :as string]))
    (:gen-class))

(defn render [t]
      (apply str t))

(deftemplate index "fileupload/index.html" [])
(deftemplate upload-success "fileupload/success.html" [])

(defn upload-file-json
  [file]
  (let [new-name (file :filename)];;(str (java.util.UUID/randomUUID) "."  "jpg")]
  (do
  (ds/copy (file :tempfile) (File. (str "public\\images\\" new-name)))
  {:status 200
       :headers {"Content-Type" "text/html"}
       :body (html (str 
		"<script>try {top.nicUploadButton.statusCb({\"done\":1,\"url\":\"/images/"
		new-name 
		"\"});} catch(e) { alert(e.message); }</script>"))})))
  
(defn upload-file
  [file]
  (let [new-name (file :filename)];;(str (java.util.UUID/randomUUID) "."  "jpg")]
  (ds/copy (file :tempfile) (File. (str "public\\images\\" new-name)))
  (println new-name)
  (render (upload-success))))  
	
(defroutes public-routes
            (GET  "/" [] (render (index)))
			(POST "/imageUpload" {params :params} (upload-file-json (get params "nicImage")))
            (POST "/file" {params :params} (upload-file (get params "file")))
            (route/files "/" {:root "public"}))
(def app
  (-> public-routes
      wrap-params
      wrap-multipart-params))
(defn start-app []
      (future (run-jetty (var app) {:port 8081})))