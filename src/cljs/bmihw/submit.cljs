(ns bmihw.submit
  (:require [bmihw.common :refer [fb]]))

(defn submit-to-fb []
  (.log js/console fb))

(defn submit-page []
  [:div [:h2 "Submit your insult."]
   [:div [:div
          [:input {:type :text :name :username :placeholder "Username"}]
          [:br]
          [:input {:type :text :name :keyword :placeholder "Keyword"}]
          [:br]
          [:input {:type :text :name :content :placeholder "Content"}]
          [:br]
          [:input {:type :submit :value "Submit"
                   :on-click submit-to-fb}]]]])