(ns bmihw.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :refer [GET POST]]
              [cljsjs.firebase :as firebase]
              [bmihw.common :refer [stuff fb]]
              [bmihw.submit :as submit])
    (:import goog.History))

;; -------------------------
;; HOME - LOGIN PAGE
;; -------------------------
(defn auth-twitter-handler
  [error, authData]
  (if error
    (reset! stuff error)
    (do
      (reset! stuff authData)
      (session/put! :current-page #'submit/submit-page))))

(defn auth-twitter
  []
  (.authWithOAuthPopup fb
                       "twitter"
                       auth-twitter-handler))

(defn home-page
  []
  [:div [:h2 "Welcome to bmihw"]
   (if @stuff
     (session/put! :current-page #'submit/submit-page)
     [:input {:type "button" :value "Login"
              :on-click #(auth-twitter)}])
   [:div [:a {:href "#/about"} "go to about page"]]])

(defn about-page
  []
  [:div [:h2 "About bmihw"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
