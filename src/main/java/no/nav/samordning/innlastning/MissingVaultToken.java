package no.nav.samordning.innlastning;

class MissingVaultToken extends RuntimeException {
    MissingVaultToken(Throwable cause) { super("Vault token missing. Check DB env variables", cause); }
}
