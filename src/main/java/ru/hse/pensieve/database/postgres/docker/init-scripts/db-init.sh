#!bin/bash

docker exec -i postgres psql -U postgres -d postgres < init.sql