package no.nav.samordning.innlasting

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.nav.samordning.innlasting.ApplicationProperties.getFromEnvironment
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig.*
import java.util.*

internal class KafkaConfiguration(env: Map<String, String>) {

    private val bootstrapServers = getFromEnvironment(env, BOOTSTRAP_SERVERS)
    private val schemaUrl = env.getOrDefault(SCHEMA_REGISTRY_URL, "http://kafka-schema-registry.tpa:8081")
    private val saslMechanism = env.getOrDefault(SASL_MECHANISM, "PLAIN")
    private val securityProtocol = env.getOrDefault(SECURITY_PROTOCOL, "SASL_SSL")
    private val saslJaasConfig = createPlainLoginModule(
            getFromEnvironment(env, USERNAME),
            getFromEnvironment(env, PASSWORD)
    )

    private fun createPlainLoginModule(username: String, password: String) =
            """org.apache.kafka.common.security.plain.PlainLoginModule required username="$username" password="$password";"""

    private fun commonConfiguration() = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            SECURITY_PROTOCOL_CONFIG to securityProtocol,
            SaslConfigs.SASL_MECHANISM to saslMechanism,
            SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig
    )

    fun streamConfiguration() = Properties().apply {
            putAll(commonConfiguration())
            put(APPLICATION_ID_CONFIG, "samordning-hendelse-innlasting-olthn65gv3")
            put(SCHEMA_REGISTRY_URL_CONFIG, schemaUrl)
            put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde::class.java)
            put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
            put(AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    companion object {
        const val BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS"
        const val SCHEMA_REGISTRY_URL = "SCHEMA_REGISTRY_URL"
        const val USERNAME = "KAFKA_USERNAME"
        const val PASSWORD = "KAFKA_PASSWORD"
        const val SASL_MECHANISM = "KAFKA_SASL_MECHANISM"
        const val SECURITY_PROTOCOL = "KAFKA_SECURITY_PROTOCOL"
        const val SAMORDNING_HENDELSE_TOPIC = "aapen-samordning-samordningspliktigHendelse-v2"
    }
}
