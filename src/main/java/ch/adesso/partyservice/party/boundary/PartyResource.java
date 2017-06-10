package ch.adesso.partyservice.party.boundary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by aranda on 10.06.2017.
 */
@Path("parties")
public class PartyResource {
   @GET
    public String getAll(){
       return "party";
   }
}
