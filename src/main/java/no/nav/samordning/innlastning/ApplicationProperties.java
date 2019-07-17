package no.nav.samordning.innlastning;

import java.util.Map;
import java.util.Optional;

class ApplicationProperties {

    static String getFromEnvironment(Map<String, String> env, String propertyName) {
        return Optional.ofNullable(env.get(propertyName))
                .orElseThrow(() -> new MissingApplicationConfig(propertyName + " not found in environment"));
    }
}