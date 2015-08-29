(ns bmihw.manage
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :refer [GET POST]]
              [bmihw.common :refer [auth fb]]
              [cljsjs.firebase :as firebase])
    (:import goog.History))

(def events (reagent/atom []))

(defn drunk?
  [{drunk "drunk" :as event}]
  (or (nil? drunk) (= drunk "true") drunk))
  

(defn add-event
  [{id :id user "username" target "target-username" keyword "keyword" content "content" :as event}]
  (let [current-user (get-in @auth ["twitter" "username"])]
    (.log js/console (str "Event User: " user ", Current User:" current-user))
    (if (= user current-user)
      (swap! events conj {:id id 
                          :user user 
                          :keyword keyword
                          :target target
                          :drunk (drunk? event)
                          :content content}))))

(defn handle-childsnapshot
  [snapshot]
  (let [key (.key snapshot)
        val (.val snapshot)]
    (if val
      (add-event (merge {:id key} (js->clj val))))))

(.on fb "child_added" handle-childsnapshot)
    
(defn manage-page
  []
  (let [who (get-in auth ["uid"])]
	  [:div [:h4 "Manage Your Insults To Those Deserving Bastards!"]
	   [:table#manage {:style {:width "100%"}}
	    [:tr
	     [:th "Drunk"][:th "Event"][:th "Insult"][:th "Keyword"] [:td who]]
     (for [event @events]
       [:tr 
        [:td (or (and (:drunk event) "Hell Yeah!") "Nope")]
        [:td (:target event)]
        [:td (:content event)]
        [:td (:keyword event)]
        [:td [:input {:type :button :value "Delete"}]]])]]))


(secretary/defroute "/manage" []
  (session/put! :current-page #'manage-page))

