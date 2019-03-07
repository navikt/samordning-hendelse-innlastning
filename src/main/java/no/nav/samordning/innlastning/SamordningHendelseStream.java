package no.nav.samordning.innlastning;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;

import java.util.Properties;

public class SamordningHendelseStream {

    static KafkaStreams build(String kafkaTopic, Properties streamProperties) {

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(kafkaTopic)
               .foreach((key, value) -> System.out.println("Key: " + key + ". Value: " + value));

        return new KafkaStreams(builder.build(), streamProperties);
    }
}
