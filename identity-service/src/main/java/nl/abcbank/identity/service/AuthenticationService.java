package nl.abcbank.identity.service;

import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceLogonRequest;
import nl.abcbank.openapi.identityservice.internal.model.IdentityServiceRegistrationRequest;

public interface AuthenticationService {

    String register(IdentityServiceRegistrationRequest identityServiceRegistrationRequest);

    void logon(IdentityServiceLogonRequest identityServiceLogonRequest);

}
