package nl.abcbank.dbmigrator.service;

import lombok.extern.slf4j.Slf4j;
import nl.abcbank.dbmigrator.model.jpa.BankAccount;
import nl.abcbank.dbmigrator.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DbMigratorServiceImpl implements DbMigratorService {

    private static final int DB_INSERT_DELAY_MS = 500;

    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public DbMigratorServiceImpl(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    @Override
    public void saveBankAccount(BankAccount bankAccount) throws InterruptedException {
        bankAccountRepository.save(bankAccount);
        Thread.sleep(DB_INSERT_DELAY_MS);
    }

}