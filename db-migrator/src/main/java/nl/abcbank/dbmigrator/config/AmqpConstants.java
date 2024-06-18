package nl.abcbank.dbmigrator.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmqpConstants {

    public static final String IDENTITY_EXCHANGE = "IdentityExchange";
    public static final String IDENTITY_QUEUE = "IdentityQueue";
    public static final String IDENTITY_BINDING_ROUTING_KEY = "Identity.#";

    public static final String DEAD_LETTER_QUEUE = "DeadletterQueue";
    public static final String DEAD_LETTER_EXCHANGE = "DeadletterExchange";
    public static final String DEAD_LETTER_BINDING_ROUTING_KEY = "#";

}
