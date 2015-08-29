(ns bmihw.submit)

(defn submit-page
  []
  [:div [:h2 "Submit your insult."]
   [:div [:form
          [:input {:type :text :name :username :placeholder "Username"}]
          [:input {:type :text :name :keyword :placeholder "Keyword"}]
          [:br]
          [:input {:type "text" :name "value"}]
          [:input {:type "submit" :value "Submit"}]]]])