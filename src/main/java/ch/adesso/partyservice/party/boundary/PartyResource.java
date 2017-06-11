package ch.adesso.partyservice.party.boundary;

import ch.adesso.partyservice.kafka.KafkaProvider;
import ch.adesso.partyservice.kafka.PartyKafkaStreams;
import ch.adesso.partyservice.party.entity.Person;
import ch.adesso.partyservice.party.entity.PersonEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.UUID;

@Path("parties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class PartyResource {

    @Inject
    private KafkaProducer<String,String> producer;

    private KafkaStreams kafkaStreams = PartyKafkaStreams.createStreams();

    @PreDestroy
    private void closeStream() {
        kafkaStreams.close();
    }

    @GET
    public Person getAll(){

       return new Person("1","Maria", "Aranda", LocalDateTime.parse("1981-12-15T00:00:00"));
    }

    @POST
    public Person createPerson(Person person) {
        UUID uuid = UUID.randomUUID();
        person.setId(uuid.toString());
      //  ProducerRecord<String, String> record = new ProducerRecord<String, String>(KafkaProvider.TOPIC, person.toString());
       // producer.send(record);
        ProducerRecord<String, String> event = new ProducerRecord<>(KafkaProvider.TOPIC, new PersonEvent(person.getId()).toString());
        producer.send(event);
        System.out.println("Person: " + person.toString());
        return person;
    }


    @GET
    @Path("/personaddress/{id}")
    public Person person(@PathParam("id") String personId) {
        final ReadOnlyKeyValueStore<String, Person> store = kafkaStreams.store(PartyKafkaStreams.PERSON_STORE,
                QueryableStoreTypes.<String, Person>keyValueStore());
        final Person person = store.get(personId);
        if (person == null) {
            throw new NotFoundException(String.format("Person with id [%d] was not found", personId));
        }

        return person;
    }
}
