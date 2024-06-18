package nl.abcbank.dbmigrator.service;

import java.time.LocalDate;
import nl.abcbank.dbmigrator.model.jpa.BankAccount;
import nl.abcbank.dbmigrator.repository.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class DbMigratorServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private DbMigratorServiceImpl dbMigratorService;

    @Test
    void shouldSaveBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setName("Keano van Dongen");
        bankAccount.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        bankAccount.setDob(LocalDate.of(2020, 10, 10));
        bankAccount.setDocumentNr("vbkpjcnchg6p");
        bankAccount.setIban("NL31ABNC1000000000");
        bankAccount.setAccountNumber(1000000000L);

        assertDoesNotThrow(() -> dbMigratorService.saveBankAccount(bankAccount));
    }

}

