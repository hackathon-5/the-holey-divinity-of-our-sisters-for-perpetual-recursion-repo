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
  [creds insult r baos]
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

(defn troll
  [insult]
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
                        :callbacks (AsyncStreamingCallback. (partial troll-em creds insult)
                                                            (comp println response-return-everything)
                                                            exception-print)))))
