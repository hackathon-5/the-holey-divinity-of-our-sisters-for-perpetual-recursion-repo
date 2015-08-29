(ns bmihw.submit
  (:require [bmihw.common :refer [auth fb]]
            [ajax.core :refer [GET]]))

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

(defn get-friends! []
  (GET "/twitter-friends"
       {:params {:username (get-in @auth ["twitter" "username"])}
        :handler (fn [response-str]
                   (let [response-edn (cljs.reader/read-string response-str)]
                     (.log js/console response-edn)))
        :error-handler (fn [{:keys [status status-text]}]
                         (js/alert (str "ERROR: " status " " status-text)))}))

(defn submit-page []
  (get-friends!)
  (let [state (atom {})]
    [:div [:h2 "Schedule your insult."]
     [:div [:div
            "When a tweet is sent by "
            [:input {:type :text :name :target-username :placeholder "Username" :class "control small"
                     :on-change #(update-state! state :target-username %)}]
            [:br]
            "and/or a tweet contains "
            [:input {:type :text :name :keyword :placeholder "Keyword" :class "control small"
                     :on-change #(update-state! state :keyword %)}]
            [:br]
            "write the following tweet:"
            [:br]
            [:input {:type :text :name :content :placeholder "Content" :class "control big"
                     :on-change #(update-state! state :content %)}]
            [:br]
            [:button {:id "submit" :class "control"
                      :on-click #(submit-to-fb! @state)}
             "SCHEDULE IT!"]]
				   [:br]
				   [:br] 
				   [:div [:a {:href "#/manage"} "Manage Submissions"]]]]))
