package nl.abcbank.apigateway.service;

import java.util.Collections;
import java.util.List;
import nl.abcbank.apigateway.config.IdentityServiceConfig;
import nl.abcbank.apigateway.exception.ServiceException;
import nl.abcbank.apigateway.model.jpa.BankAccount;
import nl.abcbank.apigateway.model.rest.BankAccountCredentials;
import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationRequest;
import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationResponse;
import nl.abcbank.openapi.apigateway.external.model.LogonRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private IdentityServiceConfig identityServiceConfig;

    @Captor
    private ArgumentCaptor<BankAccount> bankAccountCaptor;

    @Captor
    private ArgumentCaptor<List<BankAccount>> bankAccountsCaptor;

    @Captor
    private ArgumentCaptor<BankAccountCredentials> bankAccountCredentialsCaptor;

    @InjectMocks
    private AuthenticationServiceImpl bankAccountService;

    @Test
    void shouldRegister() {
        AccountRegistrationResponse accountRegistrationResponse = new AccountRegistrationResponse();
        accountRegistrationResponse.setPassword("some-password");

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(accountRegistrationResponse);
        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        AccountRegistrationRequest request = new AccountRegistrationRequest();
        request.setUserName("some-username");
        assertEquals("some-password", bankAccountService.register(request));
    }

    @Test
    void shouldFailRegistrationForExistingUserName() {
        HttpServerErrorException httpServerErrorException = new HttpServerErrorException("User name is in use",
                HttpStatus.CONFLICT, "", null, "{\"message\": \"User name is in use\"}".getBytes(), null);
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(httpServerErrorException);

        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        AccountRegistrationRequest request = new AccountRegistrationRequest();
        request.setUserName("some-username");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                bankAccountService.register(request)
        );

        assertAll(
                () -> assertEquals("User name is in use", exception.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, exception.getStatus()),
                () -> assertEquals(httpServerErrorException, exception.getException())
        );
    }

    @Test
    void shouldRegistrationIfResponseIsNull() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        AccountRegistrationRequest request = new AccountRegistrationRequest();
        request.setUserName("some-username");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                bankAccountService.register(request)
        );

        assertAll(
                () -> assertEquals("Internal server error", exception.getMessage()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus()),
                () -> assertNull(exception.getException())
        );
    }

    @Test
    void shouldFailRegistrationForIllegalArgumentException() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new IllegalArgumentException("Some exception"));
        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        AccountRegistrationRequest request = new AccountRegistrationRequest();
        request.setUserName("some-username");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                bankAccountService.register(request)
        );

        assertAll(
                () -> assertEquals("Internal server error", exception.getMessage()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus()),
                () -> assertEquals("Some exception", exception.getException().getMessage())
        );
    }

    @Test
    void shouldLogon() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(Void.class);
        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        LogonRequest request = new LogonRequest();
        request.setUserName("keano");
        request.setPassword("some-password");

        assertDoesNotThrow(() -> bankAccountService.logon(request));
    }

    @Test
    void shouldFailLogonWithWrongPassword() {
        HttpServerErrorException httpServerErrorException = new HttpServerErrorException("Wrong username or password",
                HttpStatus.UNAUTHORIZED, "", null, null, null);
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(httpServerErrorException);
        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        LogonRequest request = new LogonRequest();
        request.setUserName("keano");
        request.setPassword("some-password");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                bankAccountService.logon(request)
        );

        assertAll(
                () -> assertNull(exception.getMessage()),
                () -> assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus()),
                () -> assertEquals(httpServerErrorException, exception.getException())
        );
    }

    @Test
    void shouldFailLogonForIllegalArgumentException() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new IllegalArgumentException("Some exception"));
        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        LogonRequest request = new LogonRequest();
        request.setUserName("keano");
        request.setPassword("some-password");

        ServiceException exception = assertThrows(ServiceException.class, () ->
                bankAccountService.logon(request)
        );

        assertAll(
                () -> assertEquals("Internal server error", exception.getMessage()),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus()),
                () -> assertEquals("Some exception", exception.getException().getMessage())
        );
    }

}

