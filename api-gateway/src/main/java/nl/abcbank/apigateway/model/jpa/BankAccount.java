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
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {
        @Index(columnList = "userName", unique = true),
        @Index(columnList = "password")})
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BankAccount that = (BankAccount) o;
        return Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(iban, that.iban) &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(dob, that.dob) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(accountNumber);
        result = 31 * result + Objects.hashCode(iban);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(address);
        result = 31 * result + Objects.hashCode(dob);
        result = 31 * result + Objects.hashCode(documentNr);
        result = 31 * result + Objects.hashCode(userName);
        result = 31 * result + Objects.hashCode(password);
        return result;
    }

}
