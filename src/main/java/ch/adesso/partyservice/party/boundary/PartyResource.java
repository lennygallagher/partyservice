package ch.adesso.partyservice.party.boundary;

import ch.adesso.partyservice.kafka.KafkaProvider;
import ch.adesso.partyservice.party.entity.Person;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Path("parties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class PartyResource {

    @Inject
    private KafkaProducer<String,String> producer;


    @GET
    public Person getAll(){

       return new Person("1","Maria", "Aranda", LocalDateTime.parse("1981-12-15T00:00:00"));
    }

    @POST
    public Person createPerson(Person person) {
        UUID uuid = UUID.randomUUID();
        person.setId(uuid.toString());
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(KafkaProvider.TOPIC, person.toString());
        producer.send(record);
        System.out.println("Person: " + person.toString());
        return person;
    }
}
