package nl.abcbank.dbmigrator.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
                Objects.equals(documentNr, that.documentNr);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(accountNumber);
        result = 31 * result + Objects.hashCode(iban);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(address);
        result = 31 * result + Objects.hashCode(dob);
        result = 31 * result + Objects.hashCode(documentNr);
        return result;
    }

}
