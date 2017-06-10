package ch.adesso.partyservice.party.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessorType;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Person {

    private String firstname;
    private String lastname;

    private LocalDate birthdate;


}
