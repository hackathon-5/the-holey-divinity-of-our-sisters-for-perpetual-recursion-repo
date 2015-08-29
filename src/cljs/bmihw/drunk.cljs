(ns bmihw.drunk
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :refer [GET POST]]
              [bmihw.common :refer [drunk]]))

(defn drunk-page
  []
  [:div 
    [:h1 "Are You Drunk?"]
    [:div.btn-group.btn-group-justified
	    [:a.btn.btn-lg.btn-success {:href "#/submit" 
                                 :type "button" 
                                 :on-click (fn [e] (drunk true) true)} "Wheres My Pants?"]
	    [:a.btn.btn-lg.btn-danger {:href "#/submit" 
                                :type "button"
                                :on-click (fn [e] (drunk false) true)} "Nope, I'm Just A Dick."]]])

(secretary/defroute "/drunk" []
  (session/put! :current-page #'drunk-page))
