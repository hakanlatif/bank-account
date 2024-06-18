package nl.abcbank.identity.controller;

import nl.abcbank.identity.service.AuthenticationService;
import nl.abcbank.openapi.identityservice.internal.api.AccountApi;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceLogonRequest;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceRegistrationRequest;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceRegistrationResponse;
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
    public ResponseEntity<IdentityServiceRegistrationResponse> register(IdentityServiceRegistrationRequest identityServiceRegistrationRequest) {
        String defaultPassword = authenticationService.register(identityServiceRegistrationRequest);

        IdentityServiceRegistrationResponse identityServiceRegistrationResponse = new IdentityServiceRegistrationResponse();
        identityServiceRegistrationResponse.setUserName(identityServiceRegistrationRequest.getUserName());
        identityServiceRegistrationResponse.setPassword(defaultPassword);

        return new ResponseEntity<>(identityServiceRegistrationResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> logon(IdentityServiceLogonRequest identityServiceLogonRequest) {
        authenticationService.logon(identityServiceLogonRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
