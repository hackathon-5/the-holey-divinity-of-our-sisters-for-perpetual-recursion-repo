(ns bmihw.common
  (:require [cljsjs.firebase :as firebase]))

(def auth (atom nil))
(def fb (js/Firebase. "https://bmihw.firebaseio.com"))