kill $(lsof -t -i:9000) & \
kill $(lsof -t -i:9100)
kill $(lsof -t -i:9200)