package no.nav.samordning.innlasting

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class VaultDataSourceTest {

    @Test
    fun throw_MissingApplicationConfig_when_database_url_is_missing_from_environment() {
        val testEnvironment = testEnvironmentWithouthProperty(DB_URL_ENV_KEY)
        assertThrows(
                MissingApplicationConfig::class.java
        ) { VaultDataSource(testEnvironment) }
    }

    @Test
    fun throw_MissingApplicationConfig_when_mount_path_is_missing_from_environment() {
        val testEnvironment = testEnvironmentWithouthProperty(DB_MOUNT_PATH_ENV_KEY)
        assertThrows(
                MissingApplicationConfig::class.java
        ) { VaultDataSource(testEnvironment) }
    }

    @Test
    fun throw_MissingApplicationConfig_when_database_role_is_missing_from_environment() {
        val testEnvironment = testEnvironmentWithouthProperty(DB_ROLE_ENV_KEY)
        assertThrows(
                MissingApplicationConfig::class.java
        ) { VaultDataSource(testEnvironment) }
    }

    companion object {

        private const val DB_URL_ENV_KEY = "DB_URL"
        private const val DB_MOUNT_PATH_ENV_KEY = "DB_MOUNT_PATH"
        private const val DB_ROLE_ENV_KEY = "DB_ROLE"

        private fun testEnvironmentWithouthProperty(excludedProperty: String) = mapOf(
                DB_ROLE_ENV_KEY to "bogus",
                DB_MOUNT_PATH_ENV_KEY to "bogus",
                DB_URL_ENV_KEY to "bogus") - excludedProperty
    }
}
