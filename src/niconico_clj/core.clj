(ns niconico-clj.core
  (:require [clj-http.client :as client]
            [clojure.string :as string]
            [clojure.xml :as xml])
  (:use [ring.util.codec :only [url-decode]]))

(defn get-thumbinfo [video-id]
  (:body (client/get (str "http://ext.nicovideo.jp/api/getthumbinfo/" video-id))))

(defn get-thumb [video-id]
  (:body (client/get (str "http://ext.nicovideo.jp/thumb/" video-id))))

(def cs (clj-http.cookies/cookie-store))

(defn login [mail pass]
  (client/post "https://secure.nicovideo.jp/secure/login?site=niconico"
               {:form-params {:mail_tel mail :password pass}
                :cookie-store cs}))

(defn get-flv [video-id]
  (let [result (ref {})]
    (loop
        [urls (re-seq  #"[^=&][^=&]*" (:body (client/post (str "http://flapi.nicovideo.jp/api/getflv/" video-id) {:cookie-store cs})))]
      (if (not (= () urls))
        (do
          (dosync
           (ref-set result (assoc @result (keyword (first urls)) (url-decode (second urls)))))
          (recur (rest (rest urls))))
        @result))))

(defn get-relation [page sort order video]
  (:body
   (client/get 
    (str "http://flapi.nicovideo.jp/api/getrelation?page=" page
         "&sort=" sort
         "&order=" order
         "&video=" video))))
