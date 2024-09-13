package nl.abcbank.identity.service;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import nl.abcbank.identity.config.AmqpConstants;
import nl.abcbank.identity.config.IdentityServiceConfig;
import nl.abcbank.identity.exception.ServiceException;
import nl.abcbank.identity.helper.AmqpHelper;
import nl.abcbank.identity.helper.PasswordGenerator;
import nl.abcbank.identity.model.amqp.BankAccount;
import nl.abcbank.identity.model.jpa.BankAccountCredentials;
import nl.abcbank.identity.repository.BankAccountCredentialsRepository;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceLogonRequest;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceRegistrationRequest;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final long ACCOUNT_NUMBER_PREFIX_BASE = 1000000000;
    private static final int ABN_AMRO_ACCOUNT_NUMBER_DIGIT_LENGTH = 10;
    private static final String ABN_AMRO_BANK_CODE = "ABNC";

    private final RabbitTemplate rabbitTemplate;
    private final IdentityServiceConfig identityServiceConfig;
    private final BankAccountCredentialsRepository bankAccountCredentialsRepository;

    @Autowired
    public AuthenticationServiceImpl(RabbitTemplate rabbitTemplate, IdentityServiceConfig identityServiceConfig,
                                     BankAccountCredentialsRepository bankAccountCredentialsRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.identityServiceConfig = identityServiceConfig;
        this.bankAccountCredentialsRepository = bankAccountCredentialsRepository;
    }

    @Transactional
    @Override
    public String register(IdentityServiceRegistrationRequest identityServiceRegistrationRequest) {
        if (bankAccountCredentialsRepository.existsByUserName(identityServiceRegistrationRequest.getUserName())) {
            throw new ServiceException("User name is in use", HttpStatus.CONFLICT);
        }

        long accountNumber = calculateAccountNumber();
        String defaultPassword = PasswordGenerator.generatePassword();
        saveBankAccountCredentials(identityServiceRegistrationRequest.getUserName(), defaultPassword, accountNumber);

        BankAccount bankAccount = BankAccount.builder()
                .accountNumber(accountNumber)
                .iban(generateIban(accountNumber).toString())
                .name(identityServiceRegistrationRequest.getName())
                .address(identityServiceRegistrationRequest.getAddress())
                .dob(identityServiceRegistrationRequest.getDob())
                .documentNr(identityServiceRegistrationRequest.getDocumentNr())
                .build();

        try {
            AmqpHelper.publishMessage(rabbitTemplate, bankAccount, AmqpConstants.IDENTITY_EXCHANGE,
                    AmqpConstants.IDENTITY_BINDING_ROUTING_KEY);
            // This nested catch block ensures that exceptions including (unexpected) unchecked exceptions that might
            // be triggered by RabbitMQ library handled properly to transfer all failed messages to DLQ to not lose message
        } catch (Exception e) {
            log.error("Unable to send registered bank account to DB migrator");
            throw new ServiceException("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        return defaultPassword;
    }

    @Override
    public void logon(IdentityServiceLogonRequest identityServiceLogonRequest) {
        if (!bankAccountCredentialsRepository.existsByUserNameAndPassword(
                identityServiceLogonRequest.getUserName(), identityServiceLogonRequest.getPassword())) {
            throw new ServiceException(HttpStatus.UNAUTHORIZED);
        }
    }

    private long calculateAccountNumber() {
        Long maxAccountNumber = bankAccountCredentialsRepository.getMaxAccountNumber();
        if (maxAccountNumber == 0) {
            return ACCOUNT_NUMBER_PREFIX_BASE + identityServiceConfig.getBankAccountBranchCode();
        }

        return maxAccountNumber + 1;
    }

    private void saveBankAccountCredentials(String username, String password, Long accountNumber) {
        BankAccountCredentials bankAccountCredentials = new BankAccountCredentials();
        bankAccountCredentials.setUserName(username);
        bankAccountCredentials.setPassword(password);
        bankAccountCredentials.setAccountNumber(accountNumber);
        bankAccountCredentialsRepository.save(bankAccountCredentials);
    }

    private Iban generateIban(@Nonnull Long accountNumber) {
        return new Iban.Builder()
                .countryCode(CountryCode.NL)
                .bankCode(ABN_AMRO_BANK_CODE)
                .accountNumber(String.format("%0" + ABN_AMRO_ACCOUNT_NUMBER_DIGIT_LENGTH + "d",
                        accountNumber))
                .build();
    }

}