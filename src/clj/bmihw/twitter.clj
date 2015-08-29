(ns bmihw.twitter
  (:require [twitter.oauth :refer [make-oauth-creds]]
            [twitter.api.restful :refer [friends-ids users-lookup]]))

(def id-cache (atom {}))
(def user-cache (atom {}))

(defn twitter-friends [{:keys [username accessToken accessTokenSecret]}]
  (try
    (let [creds (make-oauth-creds "ZJpf6hvbz1KLGRQ3kUpDUm09c"
                                  "V5y3DBoI0SxjSM0R6gN9CBs3dRgMoGF7GGwruLqMBYCFKWmIuX"
                                  accessToken
                                  accessTokenSecret)
          ids (if-let [ids (get @id-cache username)]
                ids
                (->> (friends-ids :oauth-creds creds
                                  :params {:target-screen-name username})
                     :body
                     :ids
                     (take 100)))
          _ (when-not (contains? @id-cache username)
              (swap! id-cache assoc username ids))
          old-ids (filter #(contains? @user-cache %) ids)
          new-ids (remove #(contains? @user-cache %) ids)
          friends-from-cache (vals (select-keys @user-cache old-ids))
          friends-from-server (when (seq new-ids)
                                (:body (users-lookup :oauth-creds creds
                                                     :params {:user_id new-ids})))]
      (doseq [friend friends-from-server]
        (swap! user-cache assoc (:id friend) friend))
      (pr-str (concat friends-from-cache friends-from-server)))
    (catch Exception e
      (pr-str {:error (.getMessage e)}))))