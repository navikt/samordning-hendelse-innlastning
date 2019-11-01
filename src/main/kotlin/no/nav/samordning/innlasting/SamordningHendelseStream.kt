package no.nav.samordning.innlasting

import no.nav.samordning.innlasting.database.Database
import no.nav.samordning.schema.SamordningHendelse
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import java.util.*

internal object SamordningHendelseStream {

    private val streamBuilder = StreamsBuilder()

    fun build(streamProperties: Properties, database: Database): KafkaStreams {

        val samordningHendelseStream = streamBuilder.stream<String, SamordningHendelse>(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC)

        samordningHendelseStream.mapValues { _, samordningHendelse -> SamordningHendelseMapper.toJson(samordningHendelse) }
                .foreach { tpnrKey, hendelseJson -> database.insert(hendelseJson, tpnrKey) }

        return KafkaStreams(streamBuilder.build(), streamProperties)
    }


}
