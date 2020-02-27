package no.nav.samordning.innlasting


import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.schema.SamordningHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SamordningHendelseMapperTest {

    @Test
    @Throws(Exception::class)
    fun mapToJson() {

        val samordningHendelse = SamordningHendelse().apply {
            setVedtakId(VEDTAK_ID)
            setIdentifikator(IDENTIFIKATOR)
            setYtelsesType(YTELSES_TYPE)
            setSamId(SAM_ID)
            setFom(FOM)
            setTom(TOM)
        }

        val expectedHendelse = """{"identifikator": "$IDENTIFIKATOR", "ytelsesType": "$YTELSES_TYPE", "vedtakId": "$VEDTAK_ID", "samId": "$SAM_ID", "fom": "$FOM", "tom": "$TOM"}"""
        val expectedHendelseJson = ObjectMapper().writeValueAsString(expectedHendelse)

        assertEquals(expectedHendelseJson, SamordningHendelseMapper.toJson(samordningHendelse))
    }

    companion object {

        private const val VEDTAK_ID = "JKL678"
        private const val SAM_ID = "BOGUS"
        private const val IDENTIFIKATOR = "987654321"
        private const val YTELSES_TYPE = "AAP"
        private const val FOM = "2000-01-01"
        private const val TOM = "2010-02-04"
    }
}
