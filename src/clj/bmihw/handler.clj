(ns bmihw.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [bmihw.twitter :refer [twitter-friends]]))

(def home-page
  (html
   [:html
    [:head
     [:title "Being Mean is Hard Work"]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css (if (env :dev) "css/bootstrap.css" "css/bootstrap.min.css"))
     (include-css (if (env :dev) "css/bootstrap-theme.css" "css/bootstrap-theme.min.css"))
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     [:div.panel.panel-info
      [:div.panel-heading  {:align "center"}
       [:h3.panel-title "Being Mean Is Hard Work"]]
	    [:div#app.panel-body
	     [:h3 "ClojureScript has not been compiled!"]
	     [:p "please run "
	      [:b "lein figwheel"]
	      " in order to start the compiler"]]
      [:div.panel-footer {:align "center"} "Charleston - A Great Place To Work, A Great Place To Live"
       [:br]
       [:a {:href "http://jobsearch.monster.com/search/?where=Charleston%2C%20SC"
            :target "_blank"} "What Are You Waiting For!"]]
	     (include-js "js/app.js")
	     (include-js "js/jquery-2.1.4.js")
	     (include-js  (if (env :dev) "js/bootstrap.js" "js/bootstrap.min.js"))]]]))

(defroutes routes
  (GET "/" [] home-page)
  (GET "/twitter-friends" {:keys [params]} (twitter-friends params))
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
