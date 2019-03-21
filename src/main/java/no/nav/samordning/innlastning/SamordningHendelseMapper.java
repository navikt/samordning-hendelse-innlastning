package no.nav.samordning.innlastning;

import no.nav.samordning.innlastning.database.Hendelse;
import no.nav.samordning.schema.SamordningHendelse;

class SamordningHendelseMapper {

    static Hendelse mapToHendelse(SamordningHendelse samordningHendelse) {
        Hendelse hendelse = new Hendelse();
        hendelse.setVedtakId(samordningHendelse.getVedtakId());
        hendelse.setFom(samordningHendelse.getFom());
        hendelse.setTom(samordningHendelse.getTom());
        hendelse.setIdentifikator(samordningHendelse.getIdentifikator());
        hendelse.setYtelsesType(samordningHendelse.getYtelsesType());
        return hendelse;
    }
}
