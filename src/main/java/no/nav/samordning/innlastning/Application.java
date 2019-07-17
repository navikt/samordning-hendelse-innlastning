package no.nav.samordning.innlastning;

import com.zaxxer.hikari.HikariConfig;
import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.samordning.innlastning.database.Database;
import no.nav.samordning.innlastning.database.DatasourceConfig;
import no.nav.vault.jdbc.hikaricp.VaultError;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

import static no.nav.samordning.innlastning.ApplicationProperties.*;
import static no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil.*;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final String DB_URL_ENV_KEY = "DB_URL";
    private static final String DB_MOUNT_PATH_ENV_KEY = "DB_MOUNT_PATH";
    private static final String DB_ROLE_ENV_KEY = "DB_ROLE";
    private final KafkaStreams hendelseStream;

    private volatile boolean shutdown = false;

    public static void main(String[] args) {
        Application app = new Application(System.getenv());
        app.run();
    }

    Application(Map<String, String> env) {

        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(env);
        Properties streamProperties = kafkaConfiguration.streamsConfiguration();

        String jdbcUrl = getFromEnvironment(env, DB_URL_ENV_KEY);
        String mountPath = getFromEnvironment(env, DB_MOUNT_PATH_ENV_KEY);
        String role = getFromEnvironment(env, DB_ROLE_ENV_KEY);

        Database database = null;
        try {
            HikariConfig datasourceConfig = DatasourceConfig.getDatasourceConfig(jdbcUrl);
            DataSource dataSource = createHikariDataSourceWithVaultIntegration(datasourceConfig, mountPath, role);
            database = new Database(dataSource);
        } catch (VaultError vaultError) {
            LOG.error("Database access error. Could not connect to " + jdbcUrl, vaultError);
            System.exit(1);
        } catch (RuntimeException e) {
            throw new MissingVaultToken("Vault token missing. Check DB env variables", e);
        }

        NaisHttpServer naisHttpServer = new NaisHttpServer(this::isRunning, () -> true);
        try {
            naisHttpServer.start();
        } catch (Exception e) {
            LOG.error("NaisServer failed to start", e);
            System.exit(1);
        }

        hendelseStream = SamordningHendelseStream.build(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC, streamProperties, database);
        setUncaughtStreamExceptionHandler();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void setUncaughtStreamExceptionHandler() {
        hendelseStream.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread {}, closing samordningHendelseStream", t, e);
            hendelseStream.close();
        });
    }

    private boolean isRunning() {
        return hendelseStream.state().isRunning();
    }

    void run() {
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
