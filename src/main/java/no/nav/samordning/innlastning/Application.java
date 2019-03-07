package no.nav.samordning.innlastning;

import no.nav.opptjening.nais.NaisHttpServer;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final KafkaStreams hendelseStream;

    private volatile boolean shutdown = false;

    public static void main(String[] args) {
        final Application app = new Application(System.getenv());
        app.run();
    }

    Application(Map<String, String> env) {

        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(env);
        Properties streamProperties = kafkaConfiguration.streamsConfiguration();

        NaisHttpServer naisHttpServer = new NaisHttpServer(this::isRunning, () -> true);

        try {
            naisHttpServer.start();
        } catch (Exception e) {
            LOG.error("NaisServer failed to start", e);
            System.exit(1);
        }

        hendelseStream = SamordningHendelseStream.build(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC, streamProperties);

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