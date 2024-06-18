package nl.abcbank.apigateway.service;

import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationRequest;
import nl.abcbank.openapi.apigateway.external.model.LogonRequest;

public interface AuthenticationService {

    String register(AccountRegistrationRequest accountRegistrationRequest);

    void logon(LogonRequest logonRequest);

}
