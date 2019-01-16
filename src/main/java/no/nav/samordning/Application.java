package no.nav.samordning;

import no.nav.opptjening.nais.NaisHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final NaisHttpServer naisHttpServer;

    public static void main(String[]args) {
        Application app = new Application();
        app.start();
    }

    Application() {
        naisHttpServer = new NaisHttpServer();
    }

    void start() {
        try {
            naisHttpServer.start();
        } catch (Exception e) {
            LOG.error("Application could not start", e);
            System.exit(1);
        }
    }

    void stop() throws Exception {
        naisHttpServer.stop();
    }
}
