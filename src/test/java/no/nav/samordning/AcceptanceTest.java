package no.nav.samordning;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import no.nav.common.JAASCredential;
import no.nav.common.KafkaEnvironment;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AcceptanceTest {

    private static Application app;
    private final HttpClient client = HttpClient.newHttpClient();

    private static KafkaEnvironment kafkaEnvironment;
    private static final int numberOfBrokers = 1;
    private static final List<String> TOPICS = Collections.singletonList(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC);

    private static final String USERNAME = "srvsamordning-hendelse-innlastning";
    private static final String PASSWORD = "p4zzwurd";
    private static final JAASCredential AUTHORIZED_USER = new JAASCredential(USERNAME, PASSWORD);
    private static final List<JAASCredential> USERS = Collections.singletonList(AUTHORIZED_USER);

    private static final String LOCALHOST = "http://localhost:";
    private static final String DEFAULT_PORT = "8080";
    private static final String LIVENESS_ENDPOINT = "isAlive";
    private static final String READINESS_ENDPOINT = "isReady";
    private static final String METRICS_ENDPOINT = "metrics";

    //@BeforeAll
    static void setUp() {

        kafkaEnvironment = new KafkaEnvironment(numberOfBrokers, TOPICS, true, true, USERS, false);
        kafkaEnvironment.start();
        app = new Application();
        app.start();
    }

    //@AfterAll
    static void tearDown() throws Exception {
        app.stop();
    }

    //@Test
    public void isAlive_endpoint_returns_200_OK_when_application_runs() throws Exception {
        HttpRequest request = createRequest(LIVENESS_ENDPOINT);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    //@Test
    public void isReady_endpoint_returns_200_OK_when_application_runs() throws Exception {
        HttpRequest request = createRequest(READINESS_ENDPOINT);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    //@Test
    public void metrics_endpoint_returns_200_OK_when_application_runs() throws Exception {
        HttpRequest request = createRequest(METRICS_ENDPOINT);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    private HttpRequest createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(LOCALHOST + DEFAULT_PORT + "/" + endpoint))
                .GET()
                .build();
    }

    private Producer<String, Object> createTestProducer() {
        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", kafkaEnvironment.getBrokersURL());
        producerProperties.put("key.serializer", StringSerializer.class);
        producerProperties.put("value.serializer", KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", kafkaEnvironment.getSchemaRegistry().getUrl());

        return new KafkaProducer<>(producerProperties);
    }
}
