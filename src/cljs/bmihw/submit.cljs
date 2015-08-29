(ns bmihw.submit
  (:require [bmihw.common :refer [auth fb]]
            [reagent-forms.core :refer [bind-fields]]))

(defn update-state! [state key e]
  (let [new-state (swap! state assoc key (-> e .-target .-value))
        {:keys [username keyword content]} new-state
        submit (.querySelector js/document "#submit")]
    (set! (.-disabled submit) (not (and (or (seq username) (seq keyword))
                                        (seq content))))))

(defn submit-to-fb! [state]
  (.push fb
    (clj->js (merge @state
                    (select-keys @auth ["uid" "provider"])
                    (select-keys (get @auth "twitter") ["accessToken" "accessTokenSecret" "username"])))
    (fn [error]
      (when error
        (js/alert (str "An error occurred: " error))))))

(defn submit-page []
  (let [state (atom {})]
    [:div [:h2 "Submit your insult."]
     [:div [:div
            [:input {:type :text :name :target-username :placeholder "Username"
                     :on-change #(update-state! state :target-username %)}]
            [:br]
            [:input {:type :text :name :keyword :placeholder "Keyword"
                     :on-change #(update-state! state :keyword %)}]
            [:br]
            [:input {:type :text :name :content :placeholder "Content"
                     :on-change #(update-state! state :content %)}]
            [:br]
            [:input {:type :submit :value "Submit" :id "submit" :disabled true
                     :on-click #(submit-to-fb! state)}]]]]))