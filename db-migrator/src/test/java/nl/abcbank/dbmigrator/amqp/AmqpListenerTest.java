package nl.abcbank.dbmigrator.amqp;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import nl.abcbank.dbmigrator.exception.ServiceException;
import nl.abcbank.dbmigrator.helper.XmlHelper;
import nl.abcbank.dbmigrator.model.jpa.BankAccount;
import nl.abcbank.dbmigrator.service.DbMigratorServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AmqpListenerTest {

    private static final String SOME_ERROR_MESSAGE = "Some error message";

    @Mock
    private DbMigratorServiceImpl dbMigratorService;

    @Captor
    private ArgumentCaptor<BankAccount> bankAccountCaptor;

    @InjectMocks
    private AmqpListener amqpListener;

    private MockedStatic<XmlHelper> mockedXmlHelper;

    @BeforeEach
    public void setUp() {
        mockedXmlHelper = Mockito.mockStatic(XmlHelper.class);
    }

    @AfterEach
    public void tearDown() {
        mockedXmlHelper.close();
    }

    @Test
    void shouldConsumeRegisteredBankAccounts() throws ServiceException, InterruptedException, JAXBException {
        mockedXmlHelper
                .when(XmlHelper.unmarshal(any(), any()))
                .thenCallRealMethod();

        doNothing().when(dbMigratorService).saveBankAccount(any());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        nl.abcbank.dbmigrator.model.amqp.BankAccount bankAccountAmqp = new nl.abcbank.dbmigrator.model.amqp.BankAccount();
        bankAccountAmqp.setName("Keano van Dongen");
        bankAccountAmqp.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        bankAccountAmqp.setDob(LocalDate.of(2020, 10, 10));
        bankAccountAmqp.setDocumentNr("vbkpjcnchg6p");
        bankAccountAmqp.setIban("NL13ABNC1000000000");
        bankAccountAmqp.setAccountNumber(1000000000L);

        JAXB.marshal(bankAccountAmqp, outputStream);
        Message message = new Message(outputStream.toByteArray(),
                MessagePropertiesBuilder
                        .newInstance()
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .build());
        amqpListener.consumeRegisteredBankAccounts(message);

        BankAccount expectedBankAccount = new BankAccount();
        expectedBankAccount.setName("Keano van Dongen");
        expectedBankAccount.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        expectedBankAccount.setDob(LocalDate.of(2020, 10, 10));
        expectedBankAccount.setDocumentNr("vbkpjcnchg6p");
        expectedBankAccount.setIban("NL13ABNC1000000000");
        expectedBankAccount.setAccountNumber(1000000000L);

        verify(dbMigratorService, times(1))
                .saveBankAccount(bankAccountCaptor.capture());
        assertEquals(expectedBankAccount, bankAccountCaptor.getValue());
    }

    @Test
    void shouldFailConsumingRegisteredBankAccountsWithException() throws ServiceException, InterruptedException, JAXBException {
        mockedXmlHelper
                .when(XmlHelper.unmarshal(any(), any()))
                .thenThrow(new JAXBException(SOME_ERROR_MESSAGE));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        nl.abcbank.dbmigrator.model.amqp.BankAccount bankAccountAmqp = new nl.abcbank.dbmigrator.model.amqp.BankAccount();
        bankAccountAmqp.setName("Keano van Dongen");
        bankAccountAmqp.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        bankAccountAmqp.setDob(LocalDate.of(2020, 10, 10));
        bankAccountAmqp.setDocumentNr("vbkpjcnchg6p");
        bankAccountAmqp.setIban("NL13ABNC1000000000");
        bankAccountAmqp.setAccountNumber(1000000000L);

        JAXB.marshal(bankAccountAmqp, outputStream);
        Message message = new Message(outputStream.toByteArray(),
                MessagePropertiesBuilder
                        .newInstance()
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .build());

        JAXBException exception = assertThrows(JAXBException.class, () ->
                amqpListener.consumeRegisteredBankAccounts(message));
        assertEquals(SOME_ERROR_MESSAGE, exception.getMessage());

        verify(dbMigratorService, times(0))
                .saveBankAccount(bankAccountCaptor.capture());
    }

    @Test
    void shouldFailConsumingRegisteredBankAccountsWithServiceException() throws InterruptedException, JAXBException {
        mockedXmlHelper
                .when(XmlHelper.unmarshal(any(), any()))
                .thenCallRealMethod();

        doThrow(new ServiceException(SOME_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR))
                .when(dbMigratorService).saveBankAccount(any());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        nl.abcbank.dbmigrator.model.amqp.BankAccount bankAccountAmqp = new nl.abcbank.dbmigrator.model.amqp.BankAccount();
        bankAccountAmqp.setName("Keano van Dongen");
        bankAccountAmqp.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        bankAccountAmqp.setDob(LocalDate.of(2020, 10, 10));
        bankAccountAmqp.setDocumentNr("vbkpjcnchg6p");
        bankAccountAmqp.setIban("NL13ABNC1000000000");
        bankAccountAmqp.setAccountNumber(1000000000L);

        JAXB.marshal(bankAccountAmqp, outputStream);
        Message message = new Message(outputStream.toByteArray(),
                MessagePropertiesBuilder
                        .newInstance()
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .build());

        ServiceException exception = assertThrows(ServiceException.class, () ->
                amqpListener.consumeRegisteredBankAccounts(message));

        assertAll(
                () -> assertEquals(SOME_ERROR_MESSAGE, exception.getMessage()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus())
        );
    }

}
