(ns bmihw.twitter
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful]
   [twitter.api.streaming])
  (:require
   [twitter-streaming-client.core :as twit-stream]
     [twitter.oauth :refer [make-oauth-creds]]
     [twitter.api.restful :refer [friends-ids users-lookup]]
   [clojure.data.json :as json]
   [http.async.client :as ac])
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))

(def streams (atom {}))
(def insults (atom {}))
(def id-cache (atom {}))
(def user-cache (atom {}))


(defn insult-by-keyword-and-username
  [creds insult tweet]
  (if (and (= (get insult "target-username")
              (get-in tweet ["user" "screen_name"]))
           (.contains (get tweet "text")
                      (get insult "keyword")))
    (statuses-update :oauth-creds creds
                     :params {:status (get insult "content")})))

(defn insult-by-keyword
  [creds insult tweet]
  (statuses-update :oauth-creds creds
                   :params {:status (get insult "content")}))

(defn insult-by-username
  [creds insult tweet]
  (statuses-update :oauth-creds creds
                   :params {:status (get insult "content")}))

(defn troll-em
  [snapshot creds insult r baos]
;;  (println @insult)
  (println "BAOS: " (.toString baos))
  (try
    (if-not (clojure.string/blank? (.toString baos))
      (let [tweet (json/read-str (.toString baos))]
        
        (println "SCREEN NAME: " (get-in tweet ["user" "screen_name"]))
        (println "TARGET USERNAME: " (get insult "target-username"))
        (println "TEXT: " (get tweet "text"))
        (println "KEYWORD"(get insult "keyword"))

        (if (and (not (contains? tweet "event"))
                 (not (= (get insult "username")
                         (get-in tweet ["user" "screen_name"]))))
          (cond
            (and (not (clojure.string/blank? (get insult "target-username")))
                 (not (clojure.string/blank? (get insult "keyword"))))
            (insult-by-keyword-and-username creds insult tweet)
            (not (clojure.string/blank? (get insult "target-username")))
            (insult-by-keyword creds insult tweet)
            (not (clojure.string/blank? (get insult "keyword")))
            (insult-by-username creds insult tweet)))
        
        (println "TWEET: " tweet)))
    (catch Exception e
      (println e))))

(defn sync-tweets
  []
  (loop []
    (let [queue (client/retrieve-queues stream)
          tweets (map :text (:tweet queue))]
      (if (> (count tweets) 0)
        
        nil)
      (. Thread (sleep 30000)))
    (recur)))

(defn troller
  [snapshot creds insult]
  (let [stream  (client/create-twitter-stream
                  twitter.api.streaming/statuses-sample
                  :oauth-creds tweet-cred)]
    (def )

    

    (defn -main
      "Main entry point"
      []
      (do
        (client/start-twitter-stream stream)
            (sync-tweets)))
    )
  )


(defn troll
  [snapshot insult]
  (println "TROLLING: " insult)
  (let [creds (make-oauth-creds "ZJpf6hvbz1KLGRQ3kUpDUm09c"
                                "V5y3DBoI0SxjSM0R6gN9CBs3dRgMoGF7GGwruLqMBYCFKWmIuX"
                                (get insult "accessToken")
                                (get insult "accessTokenSecret"))
        username (get insult "username")]
    (Thread/sleep 15000)
    (if (contains? @streams username)
      ((:cancel (meta (get @streams username)))))
    (swap! streams
           assoc
           username
           (user-stream :oauth-creds creds
                        :callbacks (AsyncStreamingCallback. (partial troll-em
                                                                     snapshot
                                                                     creds
                                                                     insult)
                                                            (comp println response-return-everything)
                                                            exception-print)))))


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
