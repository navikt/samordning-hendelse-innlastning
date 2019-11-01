package no.nav.samordning.innlasting

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.common.embeddedutils.ServerBase
import no.nav.samordning.innlasting.KafkaConfiguration.Companion.SAMORDNING_HENDELSE_TOPIC
import no.nav.samordning.schema.SamordningHendelse
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.lang.Integer.MAX_VALUE
import java.util.*
import java.util.Collections.emptyList
import java.util.Objects.requireNonNull

internal object KafkaTestEnvironment {
    private const val NUMBER_OF_BROKERS = 1
    private const val KAFKA_USERNAME = "srvTest"
    private const val KAFKA_PASSWORD = "opensourcedPassword"
    private const val TOPIC_NAME = SAMORDNING_HENDELSE_TOPIC
    private val TOPICS = listOf(TOPIC_NAME)

    private lateinit var kafkaEnvironment: KafkaEnvironment
    lateinit var kafkaConfiguration: KafkaConfiguration
    private lateinit var testProducer: Producer<String, SamordningHendelse>

    private val url: String
        get() = requireNonNull<ServerBase>(kafkaEnvironment.schemaRegistry).url

    private val brokersURL: String
        get() = kafkaEnvironment.brokersURL

    fun setupKafkaEnvironment() {
        kafkaEnvironment = KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, emptyList<KafkaEnvironment.TopicInfo>(), withSchemaRegistry = true, withSecurity = false, users = emptyList<JAASCredential>(), autoStart = true, brokerConfigOverrides = Properties())
        kafkaConfiguration = KafkaConfiguration(testEnvironment())
        testProducer = testProducer()
    }

    private fun testEnvironment(): Map<String, String> {
        return mapOf(
                "KAFKA_BOOTSTRAP_SERVERS" to brokersURL,
                "SCHEMA_REGISTRY_URL" to url,
                "KAFKA_USERNAME" to KAFKA_USERNAME,
                "KAFKA_PASSWORD" to KAFKA_PASSWORD,
                "KAFKA_SASL_MECHANISM" to "PLAIN",
                "KAFKA_SECURITY_PROTOCOL" to "PLAINTEXT"
        )
    }

    private fun testProducer() = KafkaProducer<String, SamordningHendelse>(
            Properties().apply {
                put(BOOTSTRAP_SERVERS_CONFIG, brokersURL)
                put(SCHEMA_REGISTRY_URL_CONFIG, url)
                put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
                put(ACKS_CONFIG, "all")
                put(RETRIES_CONFIG, MAX_VALUE)
            })

    fun populate_hendelse_topic(TPNR: String, samordningHendelse: SamordningHendelse) {
        val record = ProducerRecord(TOPIC_NAME, TPNR, samordningHendelse)
        testProducer.send(record)
        testProducer.flush()
    }
}
