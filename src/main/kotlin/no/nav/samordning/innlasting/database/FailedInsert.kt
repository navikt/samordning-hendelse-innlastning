package no.nav.samordning.innlasting.database

internal class FailedInsert(message: String, cause: Throwable) : RuntimeException(message, cause)

