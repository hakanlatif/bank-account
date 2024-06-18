package nl.abcbank.apigateway.config;

import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class IdentityServiceConfig {

    @Value("#{'${identityService.hosts}'.split(',')}")
    private List<String> identityServiceHosts;

    public int getNumOfIdentityServiceInstances() {
        return identityServiceHosts.size();
    }

}
