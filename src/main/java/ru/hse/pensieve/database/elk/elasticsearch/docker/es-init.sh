#!bin/bash

docker exec -it elasticsearch curl -k -u elastic:SHfU_X+RpYkpddZzynxN -X PUT "https://elasticsearch:9200/_index_template/users_index_template" -H "Content-Type: application/json" -d @/usr/share/elasticsearch/templates/users_template.json
docker exec -it elasticsearch curl -k -u elastic:SHfU_X+RpYkpddZzynxN -X PUT "https://elasticsearch:9200/_index_template/themes_index_template" -H "Content-Type: application/json" -d @/usr/share/elasticsearch/templates/themes_template.json
docker exec -it elasticsearch curl -k -u elastic:SHfU_X+RpYkpddZzynxN -X PUT "https://elasticsearch:9200/_index_template/posts_index_template" -H "Content-Type: application/json" -d @/usr/share/elasticsearch/templates/posts_template.json