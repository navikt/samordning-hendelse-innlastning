package no.nav.samordning.innlasting;

import no.nav.samordning.schema.SamordningHendelse;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;


import static org.junit.jupiter.api.Assertions.assertEquals;

class SamordningHendelseMapperTest {

    private static final String VEDTAK_ID = "JKL678";
    private static final String IDENTIFIKATOR = "987654321";
    private static final String YTELSES_TYPE = "AAP";
    private static final String FOM = "2000-01-01";
    private static final String TOM = "2010-02-04";

    @Test
    void mapToJson() throws Exception {

        SamordningHendelse samordningHendelse = new SamordningHendelse();
        samordningHendelse.setVedtakId(VEDTAK_ID);
        samordningHendelse.setIdentifikator(IDENTIFIKATOR);
        samordningHendelse.setYtelsesType(YTELSES_TYPE);
        samordningHendelse.setFom(FOM);
        samordningHendelse.setTom(TOM);

        String expectedHendelse = "{" +
                "\"identifikator\": \"" + IDENTIFIKATOR + "\", " +
                "\"ytelsesType\": \"" + YTELSES_TYPE + "\", " +
                "\"vedtakId\": \"" + VEDTAK_ID + "\", " +
                "\"fom\": \"" + FOM + "\", " +
                "\"tom\": \"" + TOM + "\"}";
        String expectedHendelseJson = new ObjectMapper().writeValueAsString(expectedHendelse);

        assertEquals(expectedHendelseJson, SamordningHendelseMapper.toJson(samordningHendelse));
    }
}
