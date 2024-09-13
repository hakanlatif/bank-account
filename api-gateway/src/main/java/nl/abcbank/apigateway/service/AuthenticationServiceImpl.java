package nl.abcbank.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import nl.abcbank.apigateway.config.IdentityServiceConfig;
import nl.abcbank.apigateway.exception.ServiceException;
import nl.abcbank.apigateway.helper.IdentityServiceMapper;
import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationRequest;
import nl.abcbank.openapi.apigateway.external.model.AccountRegistrationResponse;
import nl.abcbank.openapi.apigateway.external.model.ErrorMessage;
import nl.abcbank.openapi.apigateway.external.model.LogonRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String IDENTITY_SERVICE_REGISTER_URI_TEMPLATE = "http://%s/account/register";
    private static final String IDENTITY_SERVICE_LOGON_URI_TEMPLATE = "http://%s/account/logon";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final IdentityServiceConfig identityServiceConfig;
    private final RestClient restClient;

    @Autowired
    public AuthenticationServiceImpl(IdentityServiceConfig identityServiceConfig, RestClient restClient) {
        this.identityServiceConfig = identityServiceConfig;
        this.restClient = restClient;
    }

    @Override
    public String register(AccountRegistrationRequest request) {
        String url = String.format(IDENTITY_SERVICE_REGISTER_URI_TEMPLATE, getIdentityServiceAddress(request.getUserName()));

        try {
            log.debug("Sending request to register : {}", url);

            AccountRegistrationResponse response = restClient.post()
                    .uri(url)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(AccountRegistrationResponse.class)
                    .getBody();

            log.debug("Request successfully made to register : {}", url);
            if (response == null) {
                log.error("AccountRegistrationResponse is null");
                throw new ServiceException(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getPassword();
        } catch (RestClientException | IllegalArgumentException e) {
            throw getServiceException(e, url);
        }
    }

    @Override
    public void logon(LogonRequest request) {
        String url = String.format(IDENTITY_SERVICE_LOGON_URI_TEMPLATE, getIdentityServiceAddress(request.getUserName()));

        try {
            log.debug("Sending request to logon : {}", url);

            restClient.post()
                    .uri(url)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Request successfully made to logon : {}", url);
        } catch (RestClientException | IllegalArgumentException e) {
            throw getServiceException(e, url);
        }
    }

    private String getIdentityServiceAddress(@Nonnull String username) {
        int identityServicePort = IdentityServiceMapper.getUniqueIdentifierOfIdentityService
                (username, identityServiceConfig.getNumOfIdentityServiceInstances());
        return identityServiceConfig.getIdentityServiceHosts().get(identityServicePort);
    }

    private ServiceException getServiceException(Exception e, String url) {
        log.error("Unable to make request to {}", url, e);

        if (!(e instanceof HttpStatusCodeException httpStatusCodeException)) {
            return new ServiceException(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        String responseBody = httpStatusCodeException.getResponseBodyAsString();
        if (StringUtils.isEmpty(responseBody)) {
            return new ServiceException((HttpStatus) httpStatusCodeException.getStatusCode(), e);
        }

        return new ServiceException(getErrorMessage(responseBody).getMessage(),
                (HttpStatus) httpStatusCodeException.getStatusCode(), e);
    }

    // TODO: https://github.com/spring-projects/spring-framework/issues/15589
    private ErrorMessage getErrorMessage(String responseBody) {
        try {
            return OBJECT_MAPPER.readValue(responseBody, ErrorMessage.class);
        } catch (IOException e) {
            log.error("Unable to parse error message : {}", responseBody, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

}