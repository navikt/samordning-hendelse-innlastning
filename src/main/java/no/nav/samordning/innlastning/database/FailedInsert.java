package no.nav.samordning.innlastning.database;

class FailedInsert extends RuntimeException {
    FailedInsert(String message, Throwable cause) {
        super(message, cause);
    }
}

