package ch.adesso.partyservice.party.boundary;

import ch.adesso.partyservice.party.entity.Person;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;

@Path("parties")
@Produces(MediaType.APPLICATION_JSON)
public class PartyResource {

   @GET
    public Person getAll(){
       return new Person("Maria", "Aranda", LocalDate.parse("1981-12-15"));
   }
}
