package no.nav.samordning.innlastning;

class MissingApplicationConfig extends RuntimeException {
    MissingApplicationConfig(String message) { super(message); }
}
