package no.nav.samordning.innlasting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.samordning.schema.SamordningHendelse;

class SamordningHendelseMapper {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static String toJson(SamordningHendelse samordningHendelse) {
        try {
            return objectMapper.writeValueAsString(samordningHendelse.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Mapping of hendelse failed: " + samordningHendelse.toString(), e);
        }
    }
}
