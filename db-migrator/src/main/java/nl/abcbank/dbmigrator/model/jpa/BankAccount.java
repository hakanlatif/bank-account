package nl.abcbank.dbmigrator.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class BankAccount implements Serializable {

    @Id
    @Column(unique = true)
    private Long accountNumber;

    @Column(unique = true)
    private String iban;

    @Column
    private String name;

    @Column
    private String address;

    @Column
    private LocalDate dob;

    @Column
    private String documentNr;

}
