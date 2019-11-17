docker run --rm --net=host  williamyeh/wrk:4.0.2 \
-c400 -t12 -d30s http://127.0.0.1:9100/static/hello.json

docker run --rm --net=host  williamyeh/wrk:4.0.2 \
-c400 -t12 -d30s http://127.0.0.1:9200/static/hello.json