package no.nav.samordning.innlastning;

public class MissingVaultToken extends RuntimeException {
    public MissingVaultToken(String message, Throwable cause) { super(message, cause); }
}
