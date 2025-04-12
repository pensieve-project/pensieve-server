package ru.hse.pensieve.database.elk.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Base64;

@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.protocol}")
    private String protocol;

    @Value("${elasticsearch.truststore.path}")
    private String truststorePath;

    @Value("${elasticsearch.truststore.password}")
    private String truststorePassword;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        String basicAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        RestClient restClient = RestClient.builder(
                        new HttpHost(host, port, protocol))
                .setDefaultHeaders(new BasicHeader[]{
                        new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                })
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setSSLContext(createSSLContext()))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));

        return new ElasticsearchClient(transport);
    }

    private SSLContext createSSLContext() {
        try (InputStream truststoreStream = getClass().getClassLoader()
                .getResourceAsStream(truststorePath)) {

            if (truststoreStream == null) {
                throw new RuntimeException("Truststore not found.");
            }

            KeyStore truststore = KeyStore.getInstance("JKS");
            truststore.load(truststoreStream, truststorePassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(truststore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;

        } catch (Exception e) {
            throw new RuntimeException("Error: SSLContext", e);
        }
    }
}
