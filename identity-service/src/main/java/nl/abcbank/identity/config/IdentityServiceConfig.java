package nl.abcbank.identity.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class IdentityServiceConfig {

    @Value("${bankAccount.branchCode}")
    private int bankAccountBranchCode;

}
