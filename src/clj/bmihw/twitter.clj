(ns bmihw.twitter
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful]
   [twitter.api.streaming])
  (:require
   [clojure.data.json :as json]
   [http.async.client :as ac])
  (:import
      (twitter.callbacks.protocols AsyncStreamingCallback)))

(def streams (atom {}))
(def insults (atom {}))

(def id-cache (atom {}))
(def user-cache (atom {}))

(defn clean-up
  [snapshot insult]
  (-> snapshot .getRef .removeValue)
  (dissoc @streams (get insult "username")))

(defn insult-by-keyword-and-username
  [snapshot creds insult tweet]
  (if (and (= (get insult "target-username")
              (get-in tweet ["user" "screen_name"]))
           (.contains (get tweet "text")
                      (get insult "keyword")))
    (do
      (println "INSULTING K&U: " (get insult "target-username"))
      (clean-up snapshot insult)
      (statuses-update :oauth-creds creds
                       :params {:status (str "@" (get insult "target-username")
                                             " "
                                             (get insult "content"))}))))

(defn insulter-not-the-same-as-the-tweeter?
  [insult tweet]
  (not (= (get insult "username")
        (get-in tweet ["user" "screen_name"]))))

(defn keyword-is-not-blank?
  [insult]
  (not (clojure.string/blank? (get insult "keyword"))))


(defn insult-by-keyword
  [snapshot creds insult tweet]

  (println "USERNAME: " (get insult "username"))
  (println "SCREEN NAME: " (get-in tweet ["user" "screen_name"]))
  (println "TWEET: " (get tweet "text"))
  (println "KEYWORD: " (get insult "keyword"))
  (println "DIFFERENT INSULTER AND TWEETER: " (not (= (get insult "username")
                                                      (get-in tweet ["user" "screen_name"]))))
  (println "KEYWORD POPULATED: " (not (clojure.string/blank? (get insult "keyword"))) )
  (println "KEYWORD IS CONTAINED: " (.contains (get tweet "text")
                                               (get insult "keyword")))
  (if (and  (insulter-not-the-same-as-the-tweeter? insult
                                                   tweet)
            (keyword-is-not-blank? insult)
           (.contains (get tweet "text")
                      (get insult "keyword")))
    (do
      (println "INSULTING K: " (get insult "target-username"))
      (statuses-update :oauth-creds creds
                       :params {:status (str "@" (get-in tweet ["user" "screen_name"])
                                             " "
                                             (get insult "content"))})
      (clean-up snapshot insult))))

(defn insult-by-username
  [snapshot creds insult tweet]
  (if (and (not (clojure.string/blank? (get insult "target-username"))) 
           (= (get insult "target-username")
              (get-in tweet ["user" "screen_name"])))
    (do
      (println "INSULTING U: " (get insult "target-username"))
      (clean-up snapshot insult)
      (statuses-update :oauth-creds creds
                       :params {:status (str "@" (get insult "target-username")
                                             " "
                                             (get insult "content"))}))))

(defn troll-em
  [snapshot creds insult r baos]
  (println "BAOS: " (.toString baos))
  (try
    (if-not (clojure.string/blank? (.toString baos))
      (let [tweet (json/read-str (.toString baos))]
        (println "TWEET: " tweet)
        (println "INSULT: " insult)        
        (println "SCREEN NAME: " (get-in tweet ["user" "screen_name"]))
        (println "TARGET USERNAME: " (get insult "target-username"))
        (println "TEXT: " (get tweet "text"))
        (println "KEYWORD: " (get insult "keyword"))

        (if (and (not (contains? tweet "event"))
                 (not (contains? tweet "friends"))
                 (not (= (get insult "username")
                         (get-in tweet ["user" "screen_name"]))))
          (cond
            (and (not (clojure.string/blank? (get insult "target-username")))
                 (not (clojure.string/blank? (get insult "keyword"))))
            (insult-by-keyword-and-username snapshot creds insult tweet)
            (not (clojure.string/blank? (get insult "target-username")))
            (insult-by-username snapshot creds insult tweet)
            (not (clojure.string/blank? (get insult "keyword")))
            (insult-by-keyword snapshot creds insult tweet)
            ))

        ))
    (catch Exception e
      (println e)
      true)))

;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;

(defn troll
  [snapshot insult]
  (let [creds (make-oauth-creds "ZJpf6hvbz1KLGRQ3kUpDUm09c"
                                "V5y3DBoI0SxjSM0R6gN9CBs3dRgMoGF7GGwruLqMBYCFKWmIuX"
                                (get insult "accessToken")
                                (get insult "accessTokenSecret"))
        username (get insult "username")]

    (if (contains? @streams username)
      (do
        ((:cancel (meta (get @streams username))))
        (Thread/sleep 10000)
        (clean-up snapshot insult)))
    (swap! streams
           assoc
           username
           (user-stream :oauth-creds creds
                        :callbacks (AsyncStreamingCallback. (partial troll-em snapshot creds insult)
                                                            (comp println response-return-everything)
                                                                                                                        exception-print)))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;


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
