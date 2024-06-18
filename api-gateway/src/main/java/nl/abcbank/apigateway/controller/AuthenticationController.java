package nl.abcbank.apigateway.controller;

import nl.abcbank.apigateway.service.AuthenticationService;
import nl.abcbank.openapi.apigateway.external.api.AccountApi;
import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationRequest;
import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationResponse;
import nl.abcbank.openapi.apigateway.external.model.LogonRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthenticationController implements AccountApi {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public ResponseEntity<AccountRegistrationResponse> register(AccountRegistrationRequest accountRegistrationRequest) {
        String defaultPassword = authenticationService.register(accountRegistrationRequest);

        AccountRegistrationResponse accountRegistrationResponse = new AccountRegistrationResponse();
        accountRegistrationResponse.setUserName(accountRegistrationRequest.getUserName());
        accountRegistrationResponse.setPassword(defaultPassword);

        return new ResponseEntity<>(accountRegistrationResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> logon(LogonRequest logonRequest) {
        authenticationService.logon(logonRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
