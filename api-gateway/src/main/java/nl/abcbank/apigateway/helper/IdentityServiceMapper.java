package nl.abcbank.apigateway.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentityServiceMapper {

    public static int getUniqueIdentifierOfIdentityService(String username, int numOfIdentityServiceInstance) {
        return Math.abs(username.hashCode() % numOfIdentityServiceInstance);
    }

}
