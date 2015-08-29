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
(def current-event (reagent/atom nil))

(defn drunk?
  [{drunk "drunk" :as event}]
  (or (nil? drunk) (= drunk "true") drunk))
  

(defn add-event
  [{id :id user "username" target "target-username" keyword "keyword" content "content" :as event}]
  (let [current-user (get-in @auth ["twitter" "username"])]
    (.log js/console (str "Event User: " user ", Current User:" current-user))
    #_(if (= user current-user))
       (swap! events conj {:id id 
                           :user user 
                           :keyword keyword
                           :target target
                           :drunk (drunk? event)
                           :content content})));;)

(defn handle-childsnapshot
  [snapshot]
  (let [key (.key snapshot)
        val (.val snapshot)]
    (if val
      (add-event (merge {:id key} (js->clj val))))))

(declare manage-page)

(defn delete-page
  []
  (if-let [drunk (:drunk @current-event)]
    [:div 
     [:p "NOPE!!!"]
     [:p "Always do sober what you said you'd do drunk. That will teach you to keep your mouth shut."]
     [:i "- Ernest Hemmingway"]
     [:br]
     [:br]
     [:div [:a {:href "#/manage"
                :on-click (fn [e] 
                            (reset! current-event nil)
                            (session/put! :current-page #'manage-page))} "Back To Manage Submissions"]]]
    [:div
     [:p "Are you sure you want to hide the evidence?"]
     [:a {:href "#/manage"
                :on-click (fn [e]
                            (let [id (:id @current-event)
                                  ref (.child fb id)
                                  clean-events (filter (fn [e] (not (= id (:id e))))  @events)]
                              (.remove ref)
                              (reset! events clean-events)
                              (reset! current-event nil)
                              (session/put! :current-page #'manage-page)))} "Yes, I'm a pussy."]
     [:a {:href "#/manage"
                :on-click (fn [e] 
                            (reset! current-event nil)
                            (session/put! :current-page #'manage-page))} "Hell No!."]]))
  

(defn manage-page
  []
  (let [who (get-in auth ["uid"])]
	  [:div.panel.panel-default 
     [:div.panel-heading "Manage Your Insults To Those Deserving Bastards!"]
	   [:table#manage {:style {:width "100%"}}
	    [:tr
	     [:th "Drunk"][:th "Event"][:th "Insult"][:th "Keyword"] [:td who]]
     (for [event @events]
       [:tr {:key (:id event)}
        [:td (or (and (:drunk event) "Hell Yeah!") "Nope")]
        [:td (:target event)]
        [:td (:content event)]
        [:td (:keyword event)]
        [:td [:input {:type "button" 
                      :value "Delete" 
                      :on-click (fn [e] 
                                  (reset! current-event event)
                                  (session/put! :current-page #'delete-page))}]]])]]))

(.on fb "child_added" handle-childsnapshot)

(secretary/defroute "/manage" []
  (session/put! :current-page #'manage-page))
