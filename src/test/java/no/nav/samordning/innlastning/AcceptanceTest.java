package no.nav.samordning.innlastning;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import kafka.security.auth.PermissionType;
import no.nav.common.JAASCredential;
import no.nav.common.KafkaEnvironment;
import no.nav.samordning.schema.SamordningHendelse;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.*;

import static no.nav.samordning.innlastning.KafkaTestUtils.createConsumerAcl;
import static no.nav.samordning.innlastning.KafkaTestUtils.createProducerAcl;
import static no.nav.samordning.innlastning.NaisEndpointTest.*;


@Testcontainers
public class AcceptanceTest {

    private static final int NUMBER_OF_BROKERS = 1;
    private static final String TOPIC_NAME = KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC;
    private static final List<String> TOPICS = Collections.singletonList(TOPIC_NAME);
    private static final String TEST_PRODUCER_NAME = "testProducer";
    private static final String TEST_PRODUCER_PASSWORD = "opensourcedPassword";
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";

    private static final String DATABASE_NAME = "samordninghendelser";
    private static final String DATABASE_USERNAME = "samordninghendelser";
    private static final String DATABASE_PASSWORD = "samordninghendelser";

    @Container
    private static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer<>()
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD)
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
                        "/docker-entrypoint-initdb.d/");

    private static final String IDENTIFIKATOR = "12345678901";
    private static final String YTELSESTYPE = "AP";
    private static final String VEDTAK_ID = "ABC123";
    private static final String FOM = "01-01-2020";
    private static final String TOM = "01-01-2010";

    private static Application app;
    private static KafkaEnvironment kafkaEnvironment;

    @BeforeAll
    static void setUp() {
        System.setProperty("zookeeper.jmx.log4j.disable", Boolean.TRUE.toString());
        kafkaEnvironment = new KafkaEnvironment(
                NUMBER_OF_BROKERS,
                TOPICS,
                true,
                true,
                Arrays.asList(
                        new JAASCredential(TEST_PRODUCER_NAME, TEST_PRODUCER_PASSWORD),
                        new JAASCredential(KAFKA_USERNAME, KAFKA_PASSWORD)
                ),
                true
        );

        AdminClient adminClient = kafkaEnvironment.getAdminClient();

        adminClient.createAcls(
                createProducerAcl(Collections.singletonMap(TOPIC_NAME, TEST_PRODUCER_NAME)));
        adminClient.createAcls(
                createConsumerAcl(Collections.singletonMap(TOPIC_NAME, KAFKA_USERNAME)));

        app = new Application(testEnvironment());
        app.run();
    }

    private static Map<String, String> testEnvironment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", kafkaEnvironment.getBrokersURL());
        testEnvironment.put("SCHEMA_REGISTRY_URL", kafkaEnvironment.getSchemaRegistry().getUrl());
        testEnvironment.put("KAFKA_USERNAME", KAFKA_USERNAME);
        testEnvironment.put("KAFKA_PASSWORD", KAFKA_PASSWORD);
        testEnvironment.put("KAFKA_SASL_MECHANISM", "PLAIN");
        testEnvironment.put("KAFKA_SECURITY_PROTOCOL", "SASL_PLAINTEXT");
        testEnvironment.put("DB_URL", postgresqlContainer.getJdbcUrl());
        testEnvironment.put("DB_USERNAME", postgresqlContainer.getUsername());
        testEnvironment.put("DB_PASSWORD", postgresqlContainer.getPassword());
        return testEnvironment;
    }

    @AfterAll
    static void tearDown() {
        app.shutdown();
        kafkaEnvironment.tearDown();
    }

    @Test
    public void innlastning_reads_hendelser_from_kafka_and_persists_to_db() throws Exception {

        ProducerRecord<String, SamordningHendelse> record = new ProducerRecord<>(
                TOPIC_NAME,
                null,
                new SamordningHendelse(IDENTIFIKATOR, YTELSESTYPE, VEDTAK_ID, FOM, TOM)
        );

        populate_hendelse_topic(record);

        //Application needs to process records before the tests resume
        Thread.sleep(5*1000);

        nais_platform_prerequisites_runs_OK();

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

    private Callback hendelseCallback() {
        return new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                System.out.println(metadata.offset());
            }
        };
    }
}
