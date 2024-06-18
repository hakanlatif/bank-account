package nl.abcbank.apigateway.model.rest;

import jakarta.validation.Valid;

public record BankAccountCredentials(@Valid String userName, @Valid String password) {

}
