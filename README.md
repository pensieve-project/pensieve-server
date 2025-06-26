<div align="center">
    <h1>Pensieve Server</h1>
    <p>- backend part of the <a href="https://github.com/pensieve-project">Pensieve Project</a></p>
    <a href="#dependencies">Dependencies</a> •
    <a href="#how-to-build">Build</a> •
    <a href="#testing">Testing</a>
    <h2></h2>
</div>

## Dependencies

- JDK 21
- PostgreSQL
- Cassandra
- Elastic Search
- Redis
- Kafka
- Logstash

All the required databases and services can be started with Docker Compose using the provided `docker-compose.yml` file, or individually using the `[db-name]-start.sh` and (if available) `[db-name]-init.sh` scripts in each respective service folder (e.g., `postgres/docker`, `cassandra/docker`, etc).

Alternatively, you can use your own running instances, but ensure that all services are accessible using the connection settings in `application.properties`.

## How to build

1. Clone the repository:
```bash
git clone https://github.com/pensieve-project/pensieve-server.git
cd pensieve-server
```

2. Copy `application-template.properties` to `application.properties`, edit secrets and DB settings

3. Generate [Elastic Search certificate](https://www.elastic.co/docs/api/doc/elasticsearch/operation/operation-ssl-certificates), add to truststore:
```bash
keytool -importcert -alias elastic-ca -file path/to/your/ca.crt -keystore src/main/resources/certs/elastic-truststore.jks -storepass YOUR_ELASTIC_TRUSTSTORE_PASSWORD -noprompt
```

4. Build and run:
```bash
./gradlew build
./gradlew bootRun
```

## Testing

1. Copy your `application.properties` to `application-test.properties`

2. Run the tests with:

```bash
./gradlew test
```