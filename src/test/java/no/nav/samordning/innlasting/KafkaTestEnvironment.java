package no.nav.samordning.innlasting;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import no.nav.common.KafkaEnvironment;
import no.nav.samordning.schema.SamordningHendelse;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static no.nav.samordning.innlasting.KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

class KafkaTestEnvironment {
    private static final int NUMBER_OF_BROKERS = 1;
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";
    private static final String TOPIC_NAME = SAMORDNING_HENDELSE_TOPIC;
    private static final List<String> TOPICS = List.of(TOPIC_NAME);

    private static KafkaEnvironment kafkaEnvironment;
    static KafkaConfiguration kafkaConfiguration;
    private static Producer<String, SamordningHendelse> testProducer;

    static void setup() {
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, emptyList(), true, false, emptyList(), true, new Properties());
        kafkaConfiguration = new KafkaConfiguration(testEnvironment());
        testProducer = testProducer();
    }

    private static String getUrl() {
        return requireNonNull(kafkaEnvironment.getSchemaRegistry()).getUrl();
    }

    private static String getBrokersURL() {
        return kafkaEnvironment.getBrokersURL();
    }

    private static Map<String, String> testEnvironment() {
        var testEnvironment = new HashMap<String, String>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", getBrokersURL());
        testEnvironment.put("SCHEMA_REGISTRY_URL", getUrl());
        testEnvironment.put("KAFKA_USERNAME", KAFKA_USERNAME);
        testEnvironment.put("KAFKA_PASSWORD", KAFKA_PASSWORD);
        testEnvironment.put("KAFKA_SASL_MECHANISM", "PLAIN");
        testEnvironment.put("KAFKA_SECURITY_PROTOCOL", "PLAINTEXT");
        return testEnvironment;
    }

    private static Producer<String, SamordningHendelse> testProducer() {
        var producerProperties = new Properties();
        producerProperties.put(BOOTSTRAP_SERVERS_CONFIG, getBrokersURL());
        producerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, getUrl());
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put(ACKS_CONFIG, "all");
        producerProperties.put(RETRIES_CONFIG, MAX_VALUE);
        return new KafkaProducer<>(producerProperties);
    }

    static void populate_hendelse_topic(String TPNR, SamordningHendelse samordningHendelse) {
        var record = new ProducerRecord<>(TOPIC_NAME, TPNR, samordningHendelse);
        testProducer.send(record);
        testProducer.flush();
    }
}
