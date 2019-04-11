package no.nav.samordning.innlastning;

import com.zaxxer.hikari.HikariDataSource;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static no.nav.samordning.innlastning.DatabaseTestUtils.*;
import static no.nav.samordning.innlastning.NaisEndpointTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public class ComponentTest {

    private static final int NUMBER_OF_BROKERS = 1;
    private static final String TOPIC_NAME = KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC;
    private static final List<String> TOPICS = Collections.singletonList(TOPIC_NAME);
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";

    private static final String IDENTIFIKATOR = "12345678901";
    private static final String YTELSESTYPE = "AP";
    private static final String VEDTAK_ID = "ABC123";
    private static final String FOM = "01-01-2020";
    private static final String TOM = "01-01-2010";

    @Container
    private static PostgreSQLContainer postgresqlContainer = setUpPostgresContainer();

    @Container
    private static GenericContainer vaultContainer = setUpVaultContainer();

    private static Application app;
    private static KafkaEnvironment kafkaEnvironment;

    @BeforeAll
    static void setUp() throws Exception {
        System.setProperty("zookeeper.jmx.log4j.disable", Boolean.TRUE.toString());

        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, Collections.emptyList(), true, false, Collections.emptyList(), true, new Properties());
        runVaultContainerCommands(vaultContainer, postgresqlContainer.getContainerIpAddress());
        app = new Application(testEnvironment());
    }

    private static Map<String, String> testEnvironment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", kafkaEnvironment.getBrokersURL());
        testEnvironment.put("SCHEMA_REGISTRY_URL", kafkaEnvironment.getSchemaRegistry().getUrl());
        testEnvironment.put("KAFKA_USERNAME", KAFKA_USERNAME);
        testEnvironment.put("KAFKA_PASSWORD", KAFKA_PASSWORD);
        testEnvironment.put("KAFKA_SASL_MECHANISM", "PLAIN");
        testEnvironment.put("KAFKA_SECURITY_PROTOCOL", "PLAINTEXT");
        testEnvironment.put("DB_URL", postgresqlContainer.getJdbcUrl());
        testEnvironment.put("DB_MOUNT_PATH", DB_MOUNT_PATH);
        testEnvironment.put("DB_ROLE", DB_ROLE);
        return testEnvironment;
    }

    @AfterAll
    static void tearDown() {
        app.shutdown();
    }

    @Test
    void innlastning_reads_hendelser_from_kafka_and_persists_hendelse_to_db_as_json() throws Exception {

        app.run();

        SamordningHendelse samordningHendelse = new SamordningHendelse(IDENTIFIKATOR, YTELSESTYPE, VEDTAK_ID, FOM, TOM);

        String expectedHendelse = "{" +
                "\"fom\": \"" + FOM + "\", " +
                "\"tom\": \"" + TOM + "\", " +
                "\"vedtakId\": \"" + VEDTAK_ID + "\", " +
                "\"ytelsesType\": \"" + YTELSESTYPE + "\", " +
                "\"identifikator\": \"" + IDENTIFIKATOR + "\"" +
                "}";


        ProducerRecord<String, SamordningHendelse> record = new ProducerRecord<>(
                TOPIC_NAME, null, samordningHendelse
        );

        populate_hendelse_topic(record);

        //Application needs to process records before the tests resume
        Thread.sleep(5*1000);

        nais_platform_prerequisites_runs_OK();

        HikariDataSource postgresqlDatasource = createPgsqlDatasource(postgresqlContainer);
        String actualHendelse = getFirstJsonHendelseFromDb(postgresqlDatasource);

        assertEquals(expectedHendelse, actualHendelse);

    }

    private void nais_platform_prerequisites_runs_OK() throws Exception {
        isAlive_endpoint_returns_200_OK_when_application_runs();
        isReady_endpoint_returns_200_OK_when_application_runs();
        metrics_endpoint_returns_200_OK_when_application_runs();
    }

    private void populate_hendelse_topic(ProducerRecord<String, SamordningHendelse> record) {
        Producer<String, SamordningHendelse> testProducer = testProducer();
        testProducer.send(record);
        testProducer.flush();
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
}
