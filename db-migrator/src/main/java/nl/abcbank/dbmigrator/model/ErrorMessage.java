package nl.abcbank.dbmigrator.model;

import lombok.Builder;

@Builder
public record ErrorMessage(String errorMessage) {
}
