package nl.abcbank.identity.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmqpConstants {

    public static final String IDENTITY_EXCHANGE = "IdentityExchange";
    public static final String IDENTITY_BINDING_ROUTING_KEY = "Identity.#";

}
