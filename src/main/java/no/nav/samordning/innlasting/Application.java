package no.nav.samordning.innlasting;

import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.samordning.innlasting.database.Database;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;


class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final KafkaStreams hendelseStream;
    private volatile boolean shutdown = false;

    public static void main(String[] args) {
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(System.getenv());
        ApplicationDataSource vaultDataSource = new VaultDataSource(System.getenv());
        Application app = new Application(vaultDataSource, kafkaConfiguration);
        app.run();
    }

    Application(ApplicationDataSource applicationDataSource, KafkaConfiguration kafkaConfiguration) {
        Properties streamProperties = kafkaConfiguration.streamConfiguration();
        Database database = new Database(applicationDataSource.dataSource());
        NaisHttpServer naisHttpServer = new NaisHttpServer(this::isRunning, () -> true);
        try {
            naisHttpServer.start();
        } catch (Exception e) {
            LOG.error("NaisServer failed to start", e);
            System.exit(1);
        }
        hendelseStream = SamordningHendelseStream.build(streamProperties, database);
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
        if (shutdown) {
            return;
        }
        shutdown = true;
        hendelseStream.close();
    }

    public interface ApplicationDataSource {
        DataSource dataSource();
    }
}
