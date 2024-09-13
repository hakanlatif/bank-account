package nl.abcbank.dbmigrator.amqp;

import jakarta.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import nl.abcbank.dbmigrator.config.AmqpConstants;
import nl.abcbank.dbmigrator.exception.ServiceException;
import nl.abcbank.dbmigrator.helper.XmlHelper;
import nl.abcbank.dbmigrator.model.amqp.BankAccount;
import nl.abcbank.dbmigrator.service.DbMigratorService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmqpListener {

    private static final String CONTAINER_FACTORY_BEAN = "retryQueuesContainerFactory";
    private static final String ACK_MODE_AUTO = "AUTO";

    private final DbMigratorService dbMigratorService;

    @Autowired
    public AmqpListener(DbMigratorService dbMigratorService) {
        this.dbMigratorService = dbMigratorService;
    }

    @RabbitListener(queues = AmqpConstants.IDENTITY_QUEUE, containerFactory = CONTAINER_FACTORY_BEAN, ackMode = ACK_MODE_AUTO)
    public void consumeRegisteredBankAccounts(Message message) throws ServiceException, JAXBException, InterruptedException {
        try {
            BankAccount bankAccountAmqp = XmlHelper.unmarshal(message.getBody(), BankAccount.class);
            dbMigratorService.saveBankAccount(getBankAccount(bankAccountAmqp));
            // This nested catch block ensures to not lose newly registered bank account
            // in case of unexpected unchecked exception while persisting the account
        } catch (Exception e) {
            log.error("Unable to save bank account : {}", new String(message.getBody(), StandardCharsets.UTF_8), e);

            throw e;
        }
    }

    private nl.abcbank.dbmigrator.model.jpa.BankAccount getBankAccount(BankAccount bankAccountAmqp) {
        nl.abcbank.dbmigrator.model.jpa.BankAccount bankAccount = new nl.abcbank.dbmigrator.model.jpa.BankAccount();
        bankAccount.setAccountNumber(bankAccountAmqp.getAccountNumber());
        bankAccount.setIban(bankAccountAmqp.getIban());
        bankAccount.setName(bankAccountAmqp.getName());
        bankAccount.setAddress(bankAccountAmqp.getAddress());
        bankAccount.setDob(bankAccountAmqp.getDob());
        bankAccount.setDocumentNr(bankAccountAmqp.getDocumentNr());
        return bankAccount;
    }

}
