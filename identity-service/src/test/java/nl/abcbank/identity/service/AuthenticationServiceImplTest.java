package nl.abcbank.identity.service;

import jakarta.xml.bind.JAXBException;
import java.time.LocalDate;
import mockit.MockUp;
import nl.abcbank.identity.config.IdentityServiceConfig;
import nl.abcbank.identity.exception.ServiceException;
import nl.abcbank.identity.helper.XmlHelper;
import nl.abcbank.identity.model.amqp.BankAccount;
import nl.abcbank.identity.model.jpa.BankAccountCredentials;
import nl.abcbank.identity.repository.BankAccountCredentialsRepository;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceLogonRequest;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.passay.PasswordGenerator;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final String DEFAULT_PASSWORD = "default-password";

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @Spy
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IdentityServiceConfig identityServiceConfig;

    @Mock
    private BankAccountCredentialsRepository bankAccountCredentialsRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    public void setup() {
        new MockUp<PasswordGenerator>() {

            @mockit.Mock
            public static String generatePassword() {
                return DEFAULT_PASSWORD;
            }
        };
    }

    @Test
    void shouldRegister() throws JAXBException {
        when(bankAccountCredentialsRepository.existsByUserName(any()))
                .thenReturn(false);

        when(identityServiceConfig.getBankAccountBranchCode()).thenReturn(1);
        BankAccountCredentials bankAccountCredentials = new BankAccountCredentials();
        when(bankAccountCredentialsRepository.save(any()))
                .thenReturn(bankAccountCredentials);

        BankAccount bankAccount = new BankAccount();
        bankAccount.setName("Keano van Dongen");
        bankAccount.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        bankAccount.setDob(LocalDate.of(2020, 10, 10));
        bankAccount.setDocumentNr("vbkpjcnchg6p");
        bankAccount.setIban("NL83ABNC1000000001");
        bankAccount.setAccountNumber(1000000001L);

        IdentityServiceRegistrationRequest request = new IdentityServiceRegistrationRequest();

        request.setName("Keano van Dongen");
        request.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        request.setDob(LocalDate.of(2020, 10, 10));
        request.setDocumentNr("vbkpjcnchg6p");
        request.setUserName("keano");

        doNothing().when(rabbitTemplate).send(anyString(), anyString(), any());
        authenticationService.register(request);

        BankAccount expectedBankAccount = BankAccount.builder()
                .accountNumber(1000000001L)
                .iban("NL83ABNC1000000001")
                .name("Keano van Dongen")
                .address("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG")
                .dob(LocalDate.of(2020, 10, 10))
                .documentNr("vbkpjcnchg6p")
                .build();

        verify(rabbitTemplate, times(1))
                .send(anyString(), anyString(), messageCaptor.capture());
        assertEquals(new Message(XmlHelper.unmarshal(expectedBankAccount)), messageCaptor.getValue());
    }

    @Test
    void shouldFailRegisteringForExistingUserName() {
        when(bankAccountCredentialsRepository.existsByUserName(any()))
                .thenReturn(true);

        IdentityServiceRegistrationRequest request = new IdentityServiceRegistrationRequest();

        request.setName("Keano van Dongen");
        request.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        request.setDob(LocalDate.of(2020, 10, 10));
        request.setDocumentNr("vbkpjcnchg6p");
        request.setUserName("keano");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                authenticationService.register(request)
        );

        assertAll(
                () -> assertEquals("User name is in use", exception.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, exception.getStatus()),
                () -> assertNull(exception.getException())
        );
    }

    @Test
    void shouldFailRegistrationWithAmqpException() {
        when(bankAccountCredentialsRepository.existsByUserName(any()))
                .thenReturn(false);

        when(identityServiceConfig.getBankAccountBranchCode()).thenReturn(1);
        doThrow(new AmqpException("Some exception"))
                .when(rabbitTemplate).send(anyString(), anyString(), any());

        BankAccount bankAccountWithIban = new BankAccount();
        bankAccountWithIban.setName("Keano van Dongen");
        bankAccountWithIban.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        bankAccountWithIban.setDob(LocalDate.of(2020, 10, 10));
        bankAccountWithIban.setDocumentNr("vbkpjcnchg6p");
        bankAccountWithIban.setIban("NL13ABNC1000000000");
        bankAccountWithIban.setAccountNumber(1000000000L);

        IdentityServiceRegistrationRequest request = new IdentityServiceRegistrationRequest();

        request.setName("Keano van Dongen");
        request.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        request.setDob(LocalDate.of(2020, 10, 10));
        request.setDocumentNr("vbkpjcnchg6p");
        request.setUserName("keano");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                authenticationService.register(request)
        );

        assertAll(
                () -> assertEquals("Internal server error", exception.getMessage()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus()),
                () -> assertEquals("Some exception", exception.getException().getMessage())
        );
    }

    @Test
    void shouldLogon() {
        IdentityServiceLogonRequest request = new IdentityServiceLogonRequest();
        request.setUserName("keano");
        request.setPassword(DEFAULT_PASSWORD);

        when(bankAccountCredentialsRepository.existsByUserNameAndPassword(any(), any()))
                .thenReturn(true);
        assertDoesNotThrow(() -> authenticationService.logon(request));
    }

    @Test
    void shouldFailLogonWithWrongPassword() {
        IdentityServiceLogonRequest request = new IdentityServiceLogonRequest();
        request.setUserName("keano");
        request.setPassword(DEFAULT_PASSWORD);
        when(bankAccountCredentialsRepository.existsByUserNameAndPassword(any(), any()))
                .thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                authenticationService.logon(request)
        );

        assertAll(
                () -> assertNull(exception.getMessage()),
                () -> assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus()),
                () -> assertNull(exception.getException())
        );
    }

}

