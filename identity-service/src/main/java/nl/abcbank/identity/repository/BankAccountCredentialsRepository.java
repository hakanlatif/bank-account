package nl.abcbank.identity.repository;

import nl.abcbank.identity.model.jpa.BankAccountCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BankAccountCredentialsRepository extends JpaRepository<BankAccountCredentials, String> {

    boolean existsByUserName(String userName);

    boolean existsByUserNameAndPassword(String userName, String password);

    @Query(value = "SELECT coalesce(max(accountNumber), 0) FROM BankAccountCredentials")
    Long getMaxAccountNumber();

}
