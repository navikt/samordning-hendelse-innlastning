package no.nav.samordning.innlastning;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class KafkaConfiguration {

    public static final String SAMORDNING_HENDELSE_TOPIC = "aapen-samordning-samordningspliktigHendelse-v1";

    public static class Properties {
        public static final String BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";
        public static final String SCHEMA_REGISTRY_URL = "SCHEMA_REGISTRY_URL";
        public static final String USERNAME = "KAFKA_USERNAME";
        public static final String PASSWORD = "KAFKA_PASSWORD";
        public static final String SASL_JAAS_CONFIG = "KAFKA_SASL_JAAS_CONFIG";
        public static final String SASL_MECHANISM = "KAFKA_SASL_MECHANISM";
        public static final String SECURITY_PROTOCOL = "KAFKA_SECURITY_PROTOCOL";
        public static final String TRUSTSTORE_LOCATION = "KAFKA_SSL_TRUSTSTORE_LOCATION";
        public static final String TRUSTSTORE_PASSWORD = "KAFKA_SSL_TRUSTSTORE_PASSWORD";
    }

    private final String bootstrapServers;
    private final String schemaUrl;
    private String securityProtocol;
    private File truststoreLocation;
    private String truststorePassword;
    private String saslMechanism;
    private String saslJaasConfig;
    private final String password;
    private final String username;


    public KafkaConfiguration(Map<String, String> env) {

        this.bootstrapServers = env.getOrDefault(Properties.BOOTSTRAP_SERVERS, "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443");

        this.schemaUrl = env.getOrDefault(Properties.SCHEMA_REGISTRY_URL, "http://kafka-schema-registry.tpa:8081");

        this.username = nullIfEmpty(env.get(Properties.USERNAME));
        this.password = nullIfEmpty(env.get(Properties.PASSWORD));

        if (this.username != null && this.password != null) {
            this.saslJaasConfig = nullIfEmpty(env.getOrDefault(Properties.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\" password=\"" + password + "\";"));
        } else {
            this.saslJaasConfig = null;
        }
        this.saslMechanism = nullIfEmpty(env.getOrDefault(Properties.SASL_MECHANISM, "PLAIN"));
        this.securityProtocol = nullIfEmpty(env.getOrDefault(Properties.SECURITY_PROTOCOL, "SASL_SSL"));

        try {
            this.truststoreLocation = resourceToFile(env.get(Properties.TRUSTSTORE_LOCATION));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Invalid truststore file", e);
        }

        this.truststorePassword = nullIfEmpty(env.get(Properties.TRUSTSTORE_PASSWORD));

    }


    private Map<String, Object> getCommonConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        if (securityProtocol != null) {
            configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        }
        if (saslMechanism != null) {
            configs.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        }
        if (saslJaasConfig != null) {
            configs.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        }
        if (truststoreLocation != null) {
            configs.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation.getAbsolutePath());
        }
        if (truststorePassword != null) {
            configs.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        }

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

    private static String nullIfEmpty(String value) {
        if ("".equals(value)) {
            return null;
        }
        return value;
    }

    private static File resourceToFile(String path) throws FileNotFoundException {
        if (path == null) {
            return null;
        }

        ClassLoader classLoader = KafkaConfiguration.class.getClassLoader();
        URL resourceUrl = classLoader.getResource(path);

        if (resourceUrl == null) {
            throw new FileNotFoundException("Resource " + path + " can not be found, or insufficient privileges");
        }

        return new File(resourceUrl.getFile());
    }

}
