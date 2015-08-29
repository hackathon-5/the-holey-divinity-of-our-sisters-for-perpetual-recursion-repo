(ns bmihw.submit
  (:require [bmihw.common :refer [auth fb]]
            [reagent-forms.core :refer [bind-fields]]))

(defn submit-to-fb [state]
  (.push fb
    (clj->js state)
    (fn [error]
      (when error
        (js/alert (str "An error occurred: " error))))))

(defn submit-page []
  (let [state (atom {})]
    [:div [:h2 "Submit your insult."]
     [:div [:div
            [:input {:type :text :name :username :placeholder "Username"
                     :on-change #(swap! state assoc :username (-> % .-target .-value))}]
            [:br]
            [:input {:type :text :name :keyword :placeholder "Keyword"
                     :on-change #(swap! state assoc :keyword (-> % .-target .-value))}]
            [:br]
            [:input {:type :text :name :content :placeholder "Content"
                     :on-change #(swap! state assoc :content (-> % .-target .-value))}]
            [:br]
            [:input {:type :submit :value "Submit"
                     :on-click #(submit-to-fb @state)}]]]]))