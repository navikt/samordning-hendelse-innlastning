package no.nav.samordning.innlastning;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import no.nav.common.KafkaEnvironment;
import no.nav.samordning.schema.SamordningHendelse;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static no.nav.samordning.innlastning.NaisEndpointTest.*;

public class AcceptanceTest {


    private static final int NUMBER_OF_BROKERS = 1;
    private static final String TOPIC_NAME = KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC;
    private static final List<String> TOPICS = Collections.singletonList(TOPIC_NAME);
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";

    private static final String IDENTIFIKATOR_1 = "12345678901";
    private static final String IDENTIFIKATOR_2 = "12345678902";
    private static final String YTELSESTYPE_1 = "AP";
    private static final String YTELSESTYPE_2 = "UFR";
    private static final String VEDTAK_ID_1 = "ABC123";
    private static final String VEDTAK_ID_2 = "CDE456";
    private static final String FOM_1 = "01-01-2020";
    private static final String FOM_2 = "31-31-2020";
    private static final String TOM_1 = "01-01-2010";
    private static final String TOM_2 = null;

    private static Application app;
    private static KafkaEnvironment kafkaEnvironment;

    @BeforeAll
    static void setUp() throws Exception {
        System.setProperty("zookeeper.jmx.log4j.disable", Boolean.TRUE.toString());
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, true, false, Collections.emptyList(), false);
        kafkaEnvironment.start();

        app = new Application(testEnvironment());
        app.run();
    }

    private static Map<String, String> testEnvironment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", kafkaEnvironment.getBrokersURL());
        testEnvironment.put("SCHEMA_REGISTRY_URL", kafkaEnvironment.getSchemaRegistry().getUrl());
        testEnvironment.put("KAFKA_USERNAME", KAFKA_USERNAME);
        testEnvironment.put("KAFKA_PASSWORD", KAFKA_PASSWORD);
        return testEnvironment;
    }

    @AfterAll
    static void tearDown() {
        kafkaEnvironment.tearDown();
        app.shutdown();
    }

    @Test
    public void innlastning_reads_hendelser_from_kafka_and_persists_to_db() throws Exception {

        populate_hendelse_topic_with_test_records();
        Thread.sleep(15*1000);

        nais_platform_prerequisites_runs_OK();
        //Thread.sleep(1000*8);
    }

    private void nais_platform_prerequisites_runs_OK() throws Exception {
        isAlive_endpoint_returns_200_OK_when_application_runs();
        isReady_endpoint_returns_200_OK_when_application_runs();
        metrics_endpoint_returns_200_OK_when_application_runs();
    }


    private void populate_hendelse_topic_with_test_records() {

        List<ProducerRecord<String, SamordningHendelse>> records = Arrays.asList(
                samordningHendelseRecordWithNullKey(IDENTIFIKATOR_1, YTELSESTYPE_1, VEDTAK_ID_1, FOM_1, TOM_1),
                samordningHendelseRecordWithNullKey(IDENTIFIKATOR_2, YTELSESTYPE_2, VEDTAK_ID_2, FOM_2, TOM_2)
        );

        Producer<String, SamordningHendelse> testProducer = testProducer();

        for(ProducerRecord<String, SamordningHendelse> record: records) {
            testProducer.send(record, hendelseProducerCallback());
        }
        testProducer.flush();
    }

    private ProducerRecord<String, SamordningHendelse> samordningHendelseRecordWithNullKey(String identifikator, String ytelsesType, String vedtakId, String fom, String tom) {
        return new ProducerRecord<>(TOPIC_NAME, null,
                new SamordningHendelse(identifikator, ytelsesType, vedtakId, fom, tom)
        );
    }

    private Producer<String, SamordningHendelse> testProducer() {

        Properties producerProperties = new Properties();
        producerProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaEnvironment.getBrokersURL());
        producerProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaEnvironment.getSchemaRegistry().getUrl());

        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProperties.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);

        return new KafkaProducer<>(producerProperties);
    }

    private static Callback hendelseProducerCallback() {
        return (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            }
            System.out.println(metadata.offset() + " " + metadata.serializedValueSize());
        };
    }
}
