package nl.abcbank.identity.model.amqp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.abcbank.identity.helper.LocalDateAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "bankAccount")
@XmlAccessorType(XmlAccessType.FIELD)
public class BankAccount {

    private Long accountNumber;
    private String iban;
    private String name;
    private String address;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dob;

    private String documentNr;

}