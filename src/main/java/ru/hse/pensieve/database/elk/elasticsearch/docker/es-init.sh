#!bin/bash

docker exec -it elasticsearch curl -k -u elastic:SHfU_X+RpYkpddZzynxN -X PUT "https://elasticsearch:9200/_index_template/users_index_template" -H "Content-Type: application/json" -d @/usr/share/logstash/templates/users_template.json