package hendelse;

import no.nav.samordning.hendelse.Hendelse;
import no.nav.samordning.hendelse.JSonToHendelseMapper;
import org.junit.Test;

import java.time.LocalDate;
import java.util.LinkedList;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

public class jSonToHendelseMapperTests {

    @Test
    public void map_From_Json_To_Hendelse() {
        LinkedList<Hendelse> expectedHendelser = new LinkedList<>();
        Hendelse expected = new Hendelse();
        expected.setYtelsesType("AAP");
        expected.setIdentifikator("12345678901");
        expected.setVedtakId("ABC123");
        expected.setFom(LocalDate.of(2020, 01, 01));
        expected.setTom(LocalDate.of(2025, 01, 01));
        expectedHendelser.add(expected);

        String json = "[{\"ytelsesType\":\"AAP\"," +
                "\"identifikator\":\"12345678901\"," +
                "\"vedtakId\":\"ABC123\"," +
                "\"fom\":\"2020-01-01\"," +
                "\"tom\":\"2025-01-01\"}]";

        LinkedList<Hendelse> hendelser = JSonToHendelseMapper.mapFromJsonToHendelse(json);

        assertThat(expected, samePropertyValuesAs(hendelser.getFirst()));
    }
}
