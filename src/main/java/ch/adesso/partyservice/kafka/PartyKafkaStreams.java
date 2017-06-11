package ch.adesso.partyservice.kafka;

import ch.adesso.partyservice.party.entity.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PartyKafkaStreams {

    public static final String PERSON_TOPIC = "person-topic";
    public static final String PERSON_STORE = "person-store";
    public static final String PERSON_ADDRESS_TOPIC = "person-address-topic";
    public static final String ADDRESS_TOPIC = "address-topic";

    private static final String BOOTSTRAP_SERVERS = "localhost:29092,localhost:39092,localhost:49092";
    private static final String APPLICATION_CONFIG_ID = "party-app";
    private static final String APPLICATION_SERVER_ID = "localhost:8081";
    private static final String STATE_DIR = "/tmp/party";


    public static KafkaStreams createStreams() {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_CONFIG_ID);
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // Provide the details of our embedded http service that we'll use to connect to this streams
        // instance and discover locations of stores.
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, APPLICATION_SERVER_ID);
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, STATE_DIR);
        // Set to earliest so we don't miss any data that arrived in the topics before the process
        // started
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Set the commit interval to 500ms so that any changes are flushed frequently and the top five
        // charts are updated with low latency.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500);
        // Allow the user to fine-tune the `metadata.max.age.ms` via Java system properties from the CLI.
        // Lowering this parameter from its default of 5 minutes to a few seconds is helpful in
        // situations where the input topic was not pre-created before running the application because
        // the application will discover a newly created topic faster.  In production, you would
        // typically not change this parameter from its default.
        String metadataMaxAgeMs = System.getProperty(ConsumerConfig.METADATA_MAX_AGE_CONFIG);
        if (metadataMaxAgeMs != null) {
            try {
                int value = Integer.parseInt(metadataMaxAgeMs);
                streamsConfiguration.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, value);
                System.out.println("Set consumer configuration " + ConsumerConfig.METADATA_MAX_AGE_CONFIG +
                        " to " + value);
            } catch (NumberFormatException ignored) {
            }
        }

        Map<String, Object> serdeProps = new HashMap<>();

        final Serializer<Person> personSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", Person.class);
        personSerializer.configure(serdeProps, false);

        final Deserializer<Person> personDeserializer = new JsonPOJODeserializer<>();
        personDeserializer.configure(serdeProps, false);

        final Serde<Person> personSerde = Serdes.serdeFrom(personSerializer, personDeserializer);

        final Serializer<PersonEvent> personEventSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", PersonEvent.class);
        personEventSerializer.configure(serdeProps, false);

        final Deserializer<PersonEvent> personEventDeserializer = new JsonPOJODeserializer<>();
        personEventDeserializer.configure(serdeProps, false);

        final Serde<PersonEvent> personEventSerde = Serdes.serdeFrom(personEventSerializer, personEventDeserializer);


        final Serializer<Address> addressSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", Address.class);
        addressSerializer.configure(serdeProps, false);

        final Deserializer<Address> addressDeserializer = new JsonPOJODeserializer<>();
        addressDeserializer.configure(serdeProps, false);

        final Serde<Address> addressSerde = Serdes.serdeFrom(addressSerializer, addressDeserializer);

        final Serializer<PersonAddress> personAddressSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", PersonAddress.class);
        personAddressSerializer.configure(serdeProps, false);

        final Deserializer<PersonAddress> personAddressDeserializer = new JsonPOJODeserializer<>();
        personAddressDeserializer.configure(serdeProps, false);

        final Serde<PersonAddress> personAddressSerde = Serdes.serdeFrom(personAddressSerializer, personAddressDeserializer);


        final KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> personEventStream = builder.stream(Serdes.String(), Serdes.String(), PERSON_TOPIC);

        KTable<String, Person> personTable = personEventStream.groupByKey()
                                 .aggregate(Person::new,
                                            (aggKey, newValue, person) -> person.applyEvent(newValue),
                                            personSerde,
                                            PERSON_STORE);

        KafkaStreams streams = new KafkaStreams(builder, new StreamsConfig(streamsConfiguration));

        streams.cleanUp();
        streams.start();

        return streams;
    }
}