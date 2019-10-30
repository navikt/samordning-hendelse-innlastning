package no.nav.samordning.innlasting

import no.nav.opptjening.nais.NaisHttpServer
import no.nav.samordning.innlasting.database.Database
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import javax.sql.DataSource
import kotlin.system.exitProcess


internal class Application(applicationDataSource: ApplicationDataSource, kafkaConfiguration: KafkaConfiguration) {
    private val hendelseStream: KafkaStreams
    @Volatile
    private var shutdown = false

    private val isRunning: Boolean
        get() = hendelseStream.state().isRunning

    init {
        val streamProperties = kafkaConfiguration.streamConfiguration()
        val database = Database(applicationDataSource.dataSource())
        val naisHttpServer = NaisHttpServer(::isRunning, "TRUE"::toBoolean)
        try {
            naisHttpServer.start()
        } catch (e: Exception) {
            LOG.error("NaisServer failed to start", e)
            exitProcess(1)
        }

        hendelseStream = SamordningHendelseStream.build(streamProperties, database)
        setUncaughtStreamExceptionHandler()
        Runtime.getRuntime().addShutdownHook(Thread(Runnable(::shutdown)))
    }

    private fun setUncaughtStreamExceptionHandler() = hendelseStream.setUncaughtExceptionHandler { t, e ->
        LOG.error("Uncaught exception in thread {}, closing samordningHendelseStream", t, e)
        hendelseStream.close()
    }

    fun run() {
        hendelseStream.setStateListener { newState, oldState ->
            LOG.debug("State change from {} to {}", oldState, newState)
            if (oldState == KafkaStreams.State.PENDING_SHUTDOWN && newState == KafkaStreams.State.NOT_RUNNING || oldState.isRunning && newState == KafkaStreams.State.ERROR) {
                LOG.warn("Stream shutdown, stopping nais http server")
                shutdown()
            }
        }
        hendelseStream.start()
    }

    fun shutdown() = if (!shutdown) {
        shutdown = true
        hendelseStream.close()
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
