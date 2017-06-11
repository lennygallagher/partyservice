package ch.adesso.partyservice.party.entity;

import ch.adesso.partyservice.LocalDateTimeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@ToString
public class Person {

    private String id;
    private String firstname;
    private String lastname;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime birthdate;



}
