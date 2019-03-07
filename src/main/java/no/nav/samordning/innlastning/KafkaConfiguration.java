package no.nav.samordning.innlastning;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.HashMap;
import java.util.Map;

public class KafkaConfiguration {

    public static final String SAMORDNING_HENDELSE_TOPIC = "aapen-samordning-samordningspliktigHendelse-v1";

    public static class Properties {
        public static final String BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";
        public static final String SCHEMA_REGISTRY_URL = "SCHEMA_REGISTRY_URL";
        public static final String USERNAME = "KAFKA_USERNAME";
        public static final String PASSWORD = "KAFKA_PASSWORD";
    }

    private final String bootstrapServers;
    private final String schemaUrl;
    private final String password;
    private final String username;

    public KafkaConfiguration(Map<String, String> env) {

        this.bootstrapServers = env.get(Properties.BOOTSTRAP_SERVERS);
        this.schemaUrl = env.getOrDefault(Properties.SCHEMA_REGISTRY_URL, "http://kafka-schema-registry.tpa:8081");

        this.username = nullIfEmpty(env.get(Properties.USERNAME));
        this.password = nullIfEmpty(env.get(Properties.PASSWORD));

    }

    private static String nullIfEmpty(String value) {
        if ("".equals(value)) {
            return null;
        }
        return value;
    }

    private Map<String, Object> getCommonConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        return configs;
    }

    public java.util.Properties streamsConfiguration() {
        Map<String, Object> configs = getCommonConfigs();
        final java.util.Properties streamsConfiguration = new java.util.Properties();
        streamsConfiguration.putAll(configs);
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "samordning-hendelse-innlastning-olthn65gv3");
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaUrl);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return streamsConfiguration;
    }

}
