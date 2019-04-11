package no.nav.samordning.innlastning;

import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.samordning.innlastning.database.Database;
import no.nav.vault.jdbc.hikaricp.VaultError;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static no.nav.samordning.innlastning.ApplicationProperties.*;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final KafkaStreams hendelseStream;

    private volatile boolean shutdown = false;

    public static void main(String[] args) {
        Application app = new Application(System.getenv());
        app.run();
    }

    Application(Map<String, String> env) {

        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(env);
        Properties streamProperties = kafkaConfiguration.streamsConfiguration();

        String jdbcUrl = getFromEnvironment(env,"DB_URL");
        String mountPath = getFromEnvironment(env, "DB_MOUNT_PATH");
        String role = getFromEnvironment(env, "DB_ROLE");

        Database database = null;
        try {
            database = new Database(jdbcUrl, mountPath, role);
        } catch (VaultError vaultError) {
            LOG.error("Database access error. Could not connect to " + jdbcUrl, vaultError);
            System.exit(1);
        }

        NaisHttpServer naisHttpServer = new NaisHttpServer(this::isRunning, () -> true);

        try {
            naisHttpServer.start();
        } catch (Exception e) {
            LOG.error("NaisServer failed to start", e);
            System.exit(1);
        }

        hendelseStream = SamordningHendelseStream.build(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC, streamProperties, database);

        hendelseStream.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread {}, closing samordningHendelseStream", t, e);
            hendelseStream.close();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private boolean isRunning() {
        return hendelseStream.state().isRunning();
    }

    public void run() {
        hendelseStream.setStateListener((newState, oldState) -> {
            LOG.debug("State change from {} to {}", oldState, newState);
            if ((oldState.equals(KafkaStreams.State.PENDING_SHUTDOWN) && newState.equals(KafkaStreams.State.NOT_RUNNING))
                    || (oldState.isRunning() && newState.equals(KafkaStreams.State.ERROR))) {
                LOG.warn("Stream shutdown, stopping nais http server");
                shutdown();
            }
        });
        hendelseStream.start();
    }

    void shutdown() {
        if(shutdown) {
            return;
        }
        shutdown = true;
        hendelseStream.close();
    }
}
