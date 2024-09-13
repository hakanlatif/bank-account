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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

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
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private ResponseEntity responseEntity;

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

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountRegistrationRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toEntity(AccountRegistrationResponse.class))
                .thenReturn(responseEntity);
        when(responseEntity.getBody())
                .thenReturn(accountRegistrationResponse);

        when(identityServiceConfig.getIdentityServiceHosts())
                .thenReturn(Collections.singletonList("localhost"));
        when(identityServiceConfig.getNumOfIdentityServiceInstances())
                .thenReturn(1);

        AccountRegistrationRequest request = new AccountRegistrationRequest();
        request.setUserName("some-username");
        assertEquals("some-password", bankAccountService.register(request));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFailRegistrationForExistingUserName() {
        HttpServerErrorException httpServerErrorException = new HttpServerErrorException("User name is in use",
                HttpStatus.CONFLICT, "", null, "{\"message\": \"User name is in use\"}".getBytes(), null);

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountRegistrationRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toEntity(AccountRegistrationResponse.class))
                .thenReturn(responseEntity);
        when(responseEntity.getBody())
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
        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountRegistrationRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toEntity(AccountRegistrationResponse.class))
                .thenReturn(responseEntity);
        when(responseEntity.getBody())
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
        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountRegistrationRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toEntity(AccountRegistrationResponse.class))
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

    @SuppressWarnings("unchecked")
    @Test
    void shouldLogon() {
        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LogonRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenReturn(responseEntity);

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

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LogonRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
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
        when(restClient.post())
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LogonRequest.class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
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

