package no.nav.samordning.innlasting

import no.nav.samordning.innlasting.KafkaConfiguration.Companion.BOOTSTRAP_SERVERS
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class KafkaConfigurationTest {

    @Test
    fun MissingApplicationConfig_is_thrown_when_bootstrap_servers_are_missing() {
        val testEnvironment = testEnvironmentWithouthProperty(BOOTSTRAP_SERVERS)
        assertThrows(
                MissingApplicationConfig::class.java
        ) { KafkaConfiguration(testEnvironment) }
    }

//    @Test
//    fun MissingApplicationConfig_is_thrown_when_kafka_username_is_missing() {
//        val testEnvironment = testEnvironmentWithouthProperty(USERNAME)
//        assertThrows(
//                MissingApplicationConfig::class.java
//        ) { KafkaConfiguration(testEnvironment) }
//    }
//
//    @Test
//    fun MissingApplicationConfig_is_thrown_when_kafka_password_is_missing() {
//        val testEnvironment = testEnvironmentWithouthProperty(PASSWORD)
//        assertThrows(
//                MissingApplicationConfig::class.java
//        ) { KafkaConfiguration(testEnvironment) }
//    }

    private fun testEnvironmentWithouthProperty(excludedProperty: String) = mutableMapOf(
            BOOTSTRAP_SERVERS to "bogus"//,
//            USERNAME to "bogus",
//            PASSWORD to "bogus"
    ) - excludedProperty
}
