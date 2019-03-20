package no.nav.samordning.innlastning;

import org.junit.Test;

import java.time.LocalDate;

import static no.nav.samordning.innlastning.StreamDataValidator.*;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamDataValidatorTest {
    
    @Test
    public void validate_valid_fom_or_tom() {
        String date = "1980-12-24";
        assertTrue(StreamDataValidator.isValidDate(date, FOM_OR_TOM_PATTERN));
    }

    @Test
    public void validate_valid_fodselsdato() {
        String date = "120900";
        assertTrue(StreamDataValidator.isValidDate(date, FODSELSDATO_PATTERN));
    }

    @Test
    public void validate_non_existing_fom_or_tom_date() {
        String date = "31-02-1950";
        assertFalse(StreamDataValidator.isValidDate(date, FOM_OR_TOM_PATTERN));
    }

    @Test
    public void validate_non_existing_fodselsdato() {
        String date = "310250";
        assertFalse(StreamDataValidator.isValidDate(date, FODSELSDATO_PATTERN));
    }

    @Test
    public void validate_fom_or_tom_in_wrong_format() {
        String date = "12-09-1900";
        assertFalse(StreamDataValidator.isValidDate(date, FOM_OR_TOM_PATTERN));
    }

    @Test
    public void validate_fodselsdato_in_wrong_format() {
        String date = "000909";
        assertFalse(StreamDataValidator.isValidDate(date, FODSELSDATO_PATTERN));
    }

    @Test
    public void validate_too_early_fom_or_tom(){
        var date = "0009-08-15";
        assertFalse(StreamDataValidator.isValidDate(date, FOM_OR_TOM_PATTERN));
    }

    @Test
    public void validate_null_fom_or_tom(){
        String date = null;
        assertFalse(StreamDataValidator.isValidDate(date, FOM_OR_TOM_PATTERN));
    }


    @Test
    public void validate_valid_identifikator() {
        String identifikator = "10027912345";
        assertTrue(StreamDataValidator.validateIdentifikator(identifikator));
    }

    @Test
    public void validate_too_short_identifikator() {
        String identifikator = "1234567891";
        assertFalse(StreamDataValidator.validateIdentifikator(identifikator));
    }

    @Test
    public void validate_too_long_identifikator() {
        String identifikator = "123456789123";
        assertFalse(StreamDataValidator.validateIdentifikator(identifikator));
    }

    @Test
    public void validate_not_all_numeric_identifikator() {
        String identifikator = "a2345678912";
        assertFalse(StreamDataValidator.validateIdentifikator(identifikator));
    }

    @Test
    public void validate_identifikator_with_non_existing_date() {
        String identifikator = "123789456";
        assertFalse(StreamDataValidator.validateIdentifikator(identifikator));
    }

    
    @Test
    public void test_not_too_early_vedtaksdato() {
        var date = LocalDate.parse("1980-09-12");
        assertTrue(StreamDataValidator.withinTimeRange(date, FOM_OR_TOM_BACKWARDS_TIME_RANGE));
    }

    @Test
    public void test_too_early_vedtaksdato() {
        var date = LocalDate.parse( "1900-09-12");
        assertFalse(StreamDataValidator.withinTimeRange(date, FOM_OR_TOM_BACKWARDS_TIME_RANGE));
    }


    @Test
    public void test_chronological_fom_or_tom() {
        String fom = "2000-01-01";
        String tom = "2012-02-02";

        assertTrue(StreamDataValidator.isChronological(fom, tom));
    }

    @Test
    public void test_non_chronological_fom_or_tom() {
        String fom = "2012-02-02";
        String tom = "2000-01-01";

        assertFalse(StreamDataValidator.isChronological(fom,tom));
    }


    @Test
    public void test_if_numeric_with_numeric() {
        String string = "1234";
        assertTrue(StreamDataValidator.isNumeric(string));
    }

    @Test
    public void test_if_numeric_with_non_numeric() {
        String string = "asdf";
        assertFalse(StreamDataValidator.isNumeric(string));
    }
}
