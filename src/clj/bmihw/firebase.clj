(ns bmihw.firebase
  (:require [bmihw.twitter :as twitter])
  (:import [com.firebase.client ChildEventListener]
           [com.firebase.client Firebase]))

(def fb (Firebase. "https://bmihw.firebaseio.com"))

(defn crackle
  []
  (.addChildEventListener fb
                          (proxy [ChildEventListener] []
                            (onCancelled [error])
                            (onChildMoved [pnsnapshot previousChildName])
                            (onChildChanged [snapshot previousChildName])
                            (onChildRemoved [snapshot])
                            (onChildAdded [snapshot previousChildName]
                              (let [insult (->> snapshot
                                                .getValue
                                                (into {}))
                                    provider (get insult "provider")]
                                (println "INSULT: " (pr-str insult) )
                                (println "PROVIDER:" (get insult "provider"))
                                (condp = provider
                                  "twitter" (twitter/troll snapshot insult)
                                  "github" (println "NOT SO FAST")))))))
