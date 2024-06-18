package nl.abcbank.dbmigrator.service;

import nl.abcbank.dbmigrator.model.jpa.BankAccount;

public interface DbMigratorService {

    void saveBankAccount(BankAccount bankAccount) throws InterruptedException;

}
