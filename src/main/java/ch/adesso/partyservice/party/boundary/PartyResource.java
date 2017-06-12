package ch.adesso.partyservice.party.boundary;

import ch.adesso.partyservice.kafka.KafkaProvider;
import ch.adesso.partyservice.party.entity.Person;
import ch.adesso.partyservice.party.entity.PersonCreatedEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("parties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class PartyResource {

    @Inject
    private KafkaProducer<String,String> producer;

    @POST
    public Person createPerson(Person person) {
        UUID uuid = UUID.randomUUID();
        person.setId(uuid.toString());
        ProducerRecord<String, String> event = new ProducerRecord<>(KafkaProvider.TOPIC,
                person.getId(), new PersonCreatedEvent(person.getId()).toJson().toString());
        producer.send(event);
        System.out.println("Person: " + person.toString());
        return person;
    }

}
