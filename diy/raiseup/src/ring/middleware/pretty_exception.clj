(ns ring.middleware.pretty-exception)



(defn pretty-print-exception
  "pretty exception"
  [e]
  (str
    "<html>
      <style type=\"text/css\">
        html {
         background:black;
         color:white;
         font-size:85%;
         font-family: Consolas, \"Liberation Mono\", Courier, monospace
        }
        span.msg {
          font-weight : 800;
          font-size : 120%;
          margin-bottom:20px;
        }
      </style>
      <body>"
    "<span class=\"msg\">" e "</span></br>"
    "<div><p>"
    (let [stacks (.getStackTrace ^Exception e)
          lengths (map #(.length (str %)) stacks)
          max-length (apply max lengths)]
      (apply str (map #(apply str (apply str (repeat (- max-length (.length (str %))) "&nbsp;")) %  "</br>") stacks)))
    "</p></div>"
    " </body>

    </html>"))

(defn wrap-pretty-exception
  "catch exception and pretty print"
  [handler & [opts]]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (assoc request :body
                       (pretty-print-exception e))))))
