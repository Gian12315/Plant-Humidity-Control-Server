(ns topicos.handler
  (:require
   [ring.logger :refer [wrap-with-logger]]
   [clojure.tools.logging :as logging]
   [topicos.controller :as controller]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [compojure.coercions :refer [as-int]]
   [ring.util.response :refer [response]]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.util.response :as res]))

(defmacro JSON-GET [& body]
  `(wrap-json-body
   (GET ~@body)))

;; Equivalent to
;; (def app-routes
;;   (routes
;;     (GET "/foo" [] "Hello Foo")))
(defroutes app-routes
  (GET "/" []
    (controller/index))
  
  (GET "/metrics" []
    (controller/metrics))

  (GET "/metrics/last" []
    (controller/metrics-last))
      
  (POST "/metrics/insert" [humidity]
    (controller/metrics-insert humidity))

  (GET "/metrics/:year" [year :<< as-int]
    (controller/metrics-year year))

  (GET "/metrics/:year/:month" [year :<< as-int month :<< as-int]
    (controller/metrics-month year month))

  (GET "/metrics/:year/:month/:day" [year :<< as-int month :<< as-int day :<< as-int is-week]

    (if (nil? is-week)
      (controller/metrics-date year month day)
      (controller/metrics-week year month day)))

  (GET "/images" []
    (controller/images))

  (GET "/images/last" []
    (controller/images-last))

  (GET "/images/pending" []
    (controller/images-pending))

  (GET "/images/some" [type sent]
    (controller/images-some type sent))
  
  (POST "/images/insert" [:as {data :params}]
    (println "==============")
    (println (get data "type"))
    (println (:tempfile (get data "type")))
    (println (get data "description"))
    (println "==============")
    (controller/images-insert (get data "type") (:tempfile (get data "image")) (get data "description")))

  (route/files "/images" {:root "images"})
  
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-multipart-params
      wrap-with-logger
      (wrap-defaults api-defaults)))
