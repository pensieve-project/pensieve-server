#!bin/bash

docker exec -it cassandra cqlsh -f /scripts/init.cql
