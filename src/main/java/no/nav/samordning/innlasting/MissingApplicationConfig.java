package no.nav.samordning.innlasting;

class MissingApplicationConfig extends RuntimeException {
    MissingApplicationConfig(String message) { super(message); }
}
