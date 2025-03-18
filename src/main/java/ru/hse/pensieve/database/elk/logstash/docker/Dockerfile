FROM logstash:8.13.0

RUN logstash-plugin install logstash-integration-jdbc

RUN mkdir -p /usr/share/logstash/drivers/
COPY cassandra_driver/. /usr/share/logstash/drivers/

RUN mkdir -p /usr/share/logstash/config/certs
COPY ./http_ca.crt /usr/share/logstash/config/certs/http_ca.crt

COPY ./logstash.conf /usr/share/logstash/pipeline/logstash.conf
