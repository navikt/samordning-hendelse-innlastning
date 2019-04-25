package no.nav.samordning.innlastning;

import no.nav.samordning.innlastning.database.Hendelse;
import no.nav.samordning.schema.SamordningHendelse;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class HendelseMapperTest {

    @Test
    public void mapToHendelse() {
        SamordningHendelse samordningHendelse = new SamordningHendelse();
        samordningHendelse.setVedtakId("JKL678");
        samordningHendelse.setIdentifikator("987654321");
        samordningHendelse.setYtelsesType("AAP");
        samordningHendelse.setFom("2000-01-01");
        samordningHendelse.setTom("2010-02-04");

        Hendelse hendelse = SamordningHendelseMapper.mapToHendelse(samordningHendelse);

        assertEquals(samordningHendelse.getVedtakId(), hendelse.getVedtakId());
        assertEquals(samordningHendelse.getIdentifikator(), hendelse.getIdentifikator());
        assertEquals(samordningHendelse.getYtelsesType(), hendelse.getYtelsesType());
        assertEquals(samordningHendelse.getFom(), hendelse.getFom());
        assertEquals(samordningHendelse.getTom(), hendelse.getTom());
    }

}
