package no.nav.samordning.innlastning;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

class StreamDataValidator {

    static final String FOM_OR_TOM_PATTERN = "uuuu-MM-dd";
    static final String FODSELSDATO_PATTERN = "ddMMuu";
    static final int FOM_OR_TOM_BACKWARDS_TIME_RANGE = 100;

    static boolean isValidDate(String date, String pattern) {
        LocalDate formattedDate;
        DateTimeFormatter dtf = DateTimeFormatter
                .ofPattern (pattern)
                .withResolverStyle(ResolverStyle.STRICT);

        try {
            formattedDate = LocalDate.parse(date, dtf);
        } catch(Exception e) {
            return false;
        }

        if(pattern.equals(FOM_OR_TOM_PATTERN)) {
            return withinTimeRange(formattedDate, FOM_OR_TOM_BACKWARDS_TIME_RANGE);
        }

        return true;
    }

    static boolean withinTimeRange(LocalDate date, int backwardsTimeRange) {
        return date.isAfter(LocalDate.now().minusYears(backwardsTimeRange));
    }

    static boolean isChronological(String firstDate, String secondDate) {
        var fom = LocalDate.parse(firstDate);
        var tom = LocalDate.parse(secondDate);
        return fom.isBefore(tom);
    }

    static boolean validateIdentifikator(String identifikator) {
        if (isValidLength(identifikator)) {
            if (isNumeric(identifikator)) {
                return isValidDate((identifikator.substring(0,6)), FODSELSDATO_PATTERN);
            }
            else {
                return false;
            }
        }
        else{
            return false;
        }
    }

    private static boolean isValidLength(String personNr) {
        return personNr.length() == 11;
    }

    static boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
