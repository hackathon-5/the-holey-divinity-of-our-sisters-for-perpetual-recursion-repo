(ns bmihw.submit
  (:require [bmihw.common :refer [auth fb]]
            [reagent-forms.core :refer [bind-fields]]))

(defn update-state! [state-atom key e]
  (swap! state-atom assoc key (-> e .-target .-value)))

(defn success! []
  (doseq [elem (-> js/document (.querySelectorAll "input") array-seq)]
    (set! (.-value elem) ""))
  (js/alert "YOU SUBMITTED SUCCESSFULLY."))

(defn submit-to-fb! [{:keys [target-username keyword content] :as state}]
  (cond
    (not (or target-username keyword))
    (js/alert "YOU NEED A USERNAME OR KEYWORD.")
    
    (not content)
    (js/alert "YOU NEED CONTENT.")
    
    :else
    (.push fb
      (clj->js (merge state
                      (select-keys @auth ["uid" "provider"])
                      (select-keys (get @auth "twitter") ["accessToken" "accessTokenSecret" "username"])))
      (fn [error]
        (if error
          (js/alert (str "ERROR: " error))
          (success!))))))

(defn submit-page []
  (let [state (atom {})]
    [:div [:h2 "Submit your insult."]
     [:div [:div
            [:input {:type :text :name :target-username :placeholder "Username" :class "control"
                     :on-change #(update-state! state :target-username %)}]
            [:br]
            [:input {:type :text :name :keyword :placeholder "Keyword" :class "control"
                     :on-change #(update-state! state :keyword %)}]
            [:br]
            [:input {:type :text :name :content :placeholder "Content" :class "control"
                     :on-change #(update-state! state :content %)}]
            [:br]
            [:button {:id "submit" :class "control"
                      :on-click #(submit-to-fb! @state)}
             "Submit"]]
				   [:br]
				   [:br] 
				   [:div [:a {:href "#/manage"} "Manage Submissions"]]]]))
