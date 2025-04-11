FROM logstash:8.13.0

RUN logstash-plugin install logstash-integration-jdbc

RUN mkdir -p /usr/share/logstash/drivers/
COPY cassandra_driver/. /usr/share/logstash/drivers/

COPY ./logstash.conf /usr/share/logstash/pipeline/logstash.conf

RUN sed -i 's/http:/https:/' /usr/share/logstash/config/logstash.yml
