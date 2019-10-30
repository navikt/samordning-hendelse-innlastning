package no.nav.samordning.innlasting

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.schema.SamordningHendelse

internal object SamordningHendelseMapper {

    private val objectMapper = ObjectMapper()

    fun toJson(samordningHendelse: SamordningHendelse) = try {
        objectMapper.writeValueAsString(samordningHendelse.toString())
    } catch (e: JsonProcessingException) {
        throw RuntimeException("Mapping of hendelse failed: $samordningHendelse", e)
    }
}
