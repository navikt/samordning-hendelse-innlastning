package no.nav.samordning.innlastning;

import no.nav.samordning.innlastning.database.Database;
import no.nav.samordning.schema.SamordningHendelse;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

class SamordningHendelseStream {

    private static final StreamsBuilder streamBuilder = new StreamsBuilder();

    static KafkaStreams build(Properties streamProperties, Database database) {

        KStream<String, SamordningHendelse> samordningHendelseStream = streamBuilder.stream(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC);

        samordningHendelseStream.mapValues((tpnrKey, samordningHendelse) -> SamordningHendelseMapper.toJson(samordningHendelse))
                .foreach((tpnrKey, hendelseJson) -> database.insert(hendelseJson, tpnrKey));

        return new KafkaStreams(streamBuilder.build(), streamProperties);
    }
}
