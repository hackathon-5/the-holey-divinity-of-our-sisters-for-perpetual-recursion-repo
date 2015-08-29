(ns bmihw.common
  (:require [cljsjs.firebase :as firebase]))

(def auth (atom nil))
(def drunk? (atom false))
(def fb (js/Firebase. "https://bmihw.firebaseio.com"))

(defn drunk
  [flag]
  (reset! drunk? (or (true? flag) (= "true" flag))))