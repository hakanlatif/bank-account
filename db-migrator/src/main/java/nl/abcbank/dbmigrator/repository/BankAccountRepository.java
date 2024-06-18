package nl.abcbank.dbmigrator.repository;

import nl.abcbank.dbmigrator.model.jpa.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

}
