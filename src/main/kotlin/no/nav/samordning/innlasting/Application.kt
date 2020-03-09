package no.nav.samordning.innlasting

import no.nav.opptjening.nais.NaisHttpServer
import no.nav.samordning.innlasting.database.Database
import org.apache.kafka.streams.KafkaStreams.State.*
import org.slf4j.LoggerFactory
import javax.sql.DataSource
import kotlin.system.exitProcess


internal class Application(applicationDataSource: ApplicationDataSource, kafkaConfiguration: KafkaConfiguration) {
    private val hendelseStream = SamordningHendelseStream.build(
            kafkaConfiguration.streamConfiguration(),
            Database(applicationDataSource.dataSource())
    ).apply {
        setUncaughtExceptionHandler { t, e ->
            LOG.error("Uncaught exception in thread {}, closing samordningHendelseStream", t, e)
            close()
        }
    }

    @Volatile
    private var shutdown = false

    private val isRunning: Boolean
        get() = hendelseStream.state().isRunning


    private val naisHttpServer = NaisHttpServer(::isRunning, "TRUE"::toBoolean).apply {
        try {
            start()
        } catch (e: Exception) {
            LOG.error("NaisServer failed to start", e)
            exitProcess(1)
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable(::shutdown)))
    }

    fun run() {
        hendelseStream.setStateListener { newState, oldState ->
            LOG.debug("State change from {} to {}", oldState, newState)
            if (oldState == PENDING_SHUTDOWN && newState == NOT_RUNNING || oldState.isRunning && newState == ERROR) {
                LOG.warn("Stream shutdown, stopping nais http server")
                shutdown()
            }
        }
        hendelseStream.start()
    }

    fun shutdown() = if (!shutdown) {
        shutdown = true
        hendelseStream.close()
        naisHttpServer.stop()
    } else Unit

    interface ApplicationDataSource {
        fun dataSource(): DataSource
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(Application::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val kafkaConfiguration = KafkaConfiguration(System.getenv())
            val vaultDataSource = VaultDataSource(System.getenv())
            val app = Application(vaultDataSource, kafkaConfiguration)
            app.run()
        }
    }
}
