package nl.abcbank.apigateway.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(indexes = {
        @Index(columnList = "userName", unique = true),
        @Index(columnList = "password")})
@Entity
public class BankAccount implements Serializable {

    @Id
    @SequenceGenerator(name = "account_num_seq", initialValue = 0, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "account_num_seq")
    private Integer accountNumber;

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

    @Column
    private String userName;

    @Column
    private String password;

}
