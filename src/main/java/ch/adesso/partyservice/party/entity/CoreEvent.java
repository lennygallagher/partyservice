package ch.adesso.partyservice.party.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by tom on 11.06.17.
 */
@Data
@AllArgsConstructor
@ToString
public class CoreEvent {
    private String id;
    private String name;

    public CoreEvent (String jsonAsString) {
        InputStream inputStream = new ByteArrayInputStream(jsonAsString.getBytes(Charset.forName("UTF-8")));
        JsonObject eventObject = Json.createReader(inputStream).readObject();
        String name = eventObject.getString("name");
        String id = eventObject.getString("id");

        this.id = id;
        this.name = name;
    }

    public JsonObject toJson(){
        return Json.createObjectBuilder()
                .add("id", id)
                .add("name", name)
                .build();
    }

}
