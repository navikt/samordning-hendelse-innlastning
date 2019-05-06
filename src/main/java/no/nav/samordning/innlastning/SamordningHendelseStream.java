package no.nav.samordning.innlastning;

import no.nav.samordning.innlastning.database.Database;
import no.nav.samordning.schema.SamordningHendelse;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

class SamordningHendelseStream {

    static KafkaStreams build(String kafkaTopic, Properties streamProperties, Database database) {

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, SamordningHendelse> samordningHendelseStream = builder.stream(kafkaTopic);

        samordningHendelseStream.mapValues((key, samordningHendelse) -> SamordningHendelseMapper.toJson(samordningHendelse))
                .foreach((key, hendelseJson) -> database.insert(hendelseJson));

        return new KafkaStreams(builder.build(), streamProperties);
    }
}
