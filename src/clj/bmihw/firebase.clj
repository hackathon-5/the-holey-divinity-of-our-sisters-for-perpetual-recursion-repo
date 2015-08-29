(ns bmihw.firebase
  (:require [bmihw.twitter :as twitter])
  (:import [com.firebase.client ChildEventListener]
           [com.firebase.client Firebase]))

(def fb (Firebase. "https://bmihw.firebaseio.com"))

(defn crackle
  []
  (.addChildEventListener fb
                          (proxy [ChildEventListener] []
                            (onChildAdded [snapshot previousChildName]
                              (let [insult (->> snapshot
                                                .getValue
                                                (into {}))]
                                (println "INSULT: " (pr-str insult) )
                                (cond = (:provider insult)
                                      "twitter" (twitter/troll insult)
                                      "github" (println "NOT SO FAST"))

                                (-> snapshot .getRef .removeValue))))))
