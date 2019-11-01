package no.nav.samordning.innlasting

internal object ApplicationProperties {

    fun getFromEnvironment(env: Map<String, String>, propertyName: String) =
            env[propertyName] ?: throw MissingApplicationConfig("$propertyName not found in environment")
}