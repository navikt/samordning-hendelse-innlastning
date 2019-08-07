package no.nav.samordning.innlasting.database;

class FailedInsert extends RuntimeException {
    FailedInsert(String message, Throwable cause) {
        super(message, cause);
    }
}

