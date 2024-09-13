package nl.abcbank.identity.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(indexes = {
        @Index(columnList = "userName", unique = true),
        @Index(columnList = "password")})
@Entity
public class BankAccountCredentials implements Serializable {

    @Id
    private String userName;

    private String password;

    @Column(unique = true)
    private Long accountNumber;

}
