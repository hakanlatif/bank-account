package nl.abcbank.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import nl.abcbank.identity.exception.ServiceException;
import nl.abcbank.identity.service.AuthenticationServiceImpl;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceLogonRequest;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    private static final String REGISTER_URI = "/account/register";
    private static final String LOGON_URI = "/account/logon";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthenticationServiceImpl bankAccountService;

    private JacksonTester<IdentityServiceRegistrationRequest> accountRegistrationRequestTester;
    private JacksonTester<IdentityServiceLogonRequest> logonRequestTester;

    @BeforeEach
    public void setup() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, OBJECT_MAPPER);
    }

    @Test
    void shouldRegisterBankAccount() throws Exception {
        IdentityServiceRegistrationRequest identityServiceRegistrationRequest = getIdentityServiceRegistrationRequest();

        when(bankAccountService.register(identityServiceRegistrationRequest)).thenReturn("default-password");

        mvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountRegistrationRequestTester.write(identityServiceRegistrationRequest).getJson()))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"userName\":\"keano\",\"password\":\"default-password\"}"));
    }

    @Test
    void shouldFailRegistrationBankAccountForExistingUsername() throws Exception {
        IdentityServiceRegistrationRequest identityServiceRegistrationRequest = getIdentityServiceRegistrationRequest();

        doThrow(new ServiceException("User name is in use", HttpStatus.CONFLICT))
                .when(bankAccountService).register(identityServiceRegistrationRequest);

        mvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountRegistrationRequestTester.write(identityServiceRegistrationRequest).getJson()))
                .andExpect(status().isConflict())
                .andExpect(content().string("{\"message\":\"User name is in use\"}"));
    }

    @Test
    void shouldFailRegistrationWithNpe() throws Exception {
        IdentityServiceRegistrationRequest identityServiceRegistrationRequest = getIdentityServiceRegistrationRequest();

        doThrow(new NullPointerException("Some error message"))
                .when(bankAccountService).register(identityServiceRegistrationRequest);

        mvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountRegistrationRequestTester.write(identityServiceRegistrationRequest).getJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"message\":\"Internal server error\"}"));
    }

    @Test
    void shouldFailRegistrationWithInvalidDob() throws Exception {
        mvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Keano van Dongen\"," +
                                "\"address\":\"3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG\"," +
                                "\"dob\": \"10-10-2020\"," +
                                "\"documentNr\":\"vbkpjcnchg6p\"," +
                                "\"userName\":\"keano\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"JSON parse error: " +
                        "Cannot deserialize value of type `java.time.LocalDate` from String \\\"10-10-2020\\\": " +
                        "Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) " +
                        "Text '10-10-2020' could not be parsed at index 0\"}"));
    }

    @Test
    void shouldLogonToAccount() throws Exception {
        IdentityServiceLogonRequest logonRequest = new IdentityServiceLogonRequest();

        logonRequest.setUserName("Keano van Dongen");
        logonRequest.setPassword("default-password");

        doNothing().when(bankAccountService).logon(logonRequest);

        mvc.perform(post(LOGON_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logonRequestTester.write(logonRequest).getJson()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailLogonAtMissingUserName() throws Exception {
        IdentityServiceLogonRequest logonRequest = new IdentityServiceLogonRequest();

        logonRequest.setPassword("default-password");

        doNothing().when(bankAccountService).logon(logonRequest);

        mvc.perform(post(LOGON_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logonRequestTester.write(logonRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("{\"message\":\"userName: must not be null\"}"));
    }

    @Test
    void shouldFailLogonAtMissingUserNameAndPassword() throws Exception {
        IdentityServiceLogonRequest logonRequest = new IdentityServiceLogonRequest();

        doNothing().when(bankAccountService).logon(logonRequest);

        mvc.perform(post(LOGON_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logonRequestTester.write(logonRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("{\"message\":\"password: must not be null, userName: must not be null\"}"));
    }

    @Test
    void shouldFailLogonWithWrongPassword() throws Exception {
        IdentityServiceLogonRequest logonRequest = new IdentityServiceLogonRequest();

        logonRequest.setUserName("Keano van Dongen");
        logonRequest.setPassword("default-password");

        doThrow(new ServiceException("Wrong username or password", HttpStatus.UNAUTHORIZED))
                .when(bankAccountService).logon(logonRequest);

        mvc.perform(post(LOGON_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logonRequestTester.write(logonRequest).getJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(content()
                        .string("{\"message\":\"Wrong username or password\"}"));
    }

    @Test
    void shouldFailLogonWithInternalServerError() throws Exception {
        IdentityServiceLogonRequest logonRequest = new IdentityServiceLogonRequest();

        logonRequest.setUserName("Keano van Dongen");
        logonRequest.setPassword("default-password");

        doThrow(new ServiceException("Some error message", HttpStatus.INTERNAL_SERVER_ERROR))
                .when(bankAccountService).logon(logonRequest);

        mvc.perform(post(LOGON_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logonRequestTester.write(logonRequest).getJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(content()
                        .string("{\"message\":\"Some error message\"}"));
    }

    private static IdentityServiceRegistrationRequest getIdentityServiceRegistrationRequest() {
        IdentityServiceRegistrationRequest identityServiceRegistrationRequest = new IdentityServiceRegistrationRequest();

        identityServiceRegistrationRequest.setName("Keano van Dongen");
        identityServiceRegistrationRequest.setAddress("3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG");
        identityServiceRegistrationRequest.setDob(LocalDate.of(2020, 10, 10));
        identityServiceRegistrationRequest.setDocumentNr("vbkpjcnchg6p");
        identityServiceRegistrationRequest.setUserName("keano");
        return identityServiceRegistrationRequest;
    }

}
