package nl.abcbank.dbmigrator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@EnableRabbit
@Configuration
public class AmqpConfig {

    private static final long RABBIT_TEMPLATE_BACKOFF_INITIAL_INTERVAL = 1000L;
    private static final int RABBIT_TEMPLATE_BACKOFF_MULTIPLIER = 2;
    private static final long RABBIT_TEMPLATE_BACKOFF_MAX_INTERVAL = 320000L;
    private static final int RABBIT_TEMPLATE_RETRY_POLICY_MAX_ATTEMPTS = 20;

    private static final int RABBIT_LISTENER_MAX_ATTEMPTS = 3;
    private static final long RABBIT_LISTENER_BACKOFF_INITIAL_INTERVAL = 5000L;
    private static final int RABBIT_LISTENER_BACKOFF_MULTIPLIER = 3;
    private static final long RABBIT_LISTENER_BACKOFF_MAX_INTERVAL = 15000L;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(RABBIT_TEMPLATE_BACKOFF_INITIAL_INTERVAL);
        backOffPolicy.setMultiplier(RABBIT_TEMPLATE_BACKOFF_MULTIPLIER);
        backOffPolicy.setMaxInterval(RABBIT_TEMPLATE_BACKOFF_MAX_INTERVAL);

        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(RABBIT_TEMPLATE_RETRY_POLICY_MAX_ATTEMPTS);

        retryTemplate.setRetryPolicy(retryPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);

        return rabbitTemplate;
    }

    @Bean
    public RetryOperationsInterceptor messagesRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(RABBIT_LISTENER_MAX_ATTEMPTS)
                .backOffOptions(RABBIT_LISTENER_BACKOFF_INITIAL_INTERVAL,
                        RABBIT_LISTENER_BACKOFF_MULTIPLIER,
                        RABBIT_LISTENER_BACKOFF_MAX_INTERVAL)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory retryQueuesContainerFactory(ConnectionFactory connectionFactory,
                                                                            RetryOperationsInterceptor messagesRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(messagesRetryInterceptor);
        factory.setErrorHandler(t -> log.error("Unable to consume message", t));
        return factory;
    }

    // dead letter configuration

    // ExchangeBuilder's default build behaviour is durable = true, declare = true and autoDelete = false.
    // this is how our exchanges are configured, so they were not explicitly defined
    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder
                .topicExchange(AmqpConstants.DEAD_LETTER_EXCHANGE)
                .build();
    }

    // QueueBuilder's default build behaviour is exclusive = false and autoDelete = false.
    // this is how our queues are configured, so they were not explicitly defined
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(AmqpConstants.DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(AmqpConstants.DEAD_LETTER_BINDING_ROUTING_KEY);
    }

    // identity configuration
    @Bean
    public Queue identityQueue() {
        return QueueBuilder
                .durable(AmqpConstants.IDENTITY_QUEUE)
                .deadLetterExchange(AmqpConstants.DEAD_LETTER_EXCHANGE)
                .build();
    }

    @Bean
    public TopicExchange identityExchange() {
        return ExchangeBuilder
                .topicExchange(AmqpConstants.IDENTITY_EXCHANGE)
                .build();
    }

    @Bean
    public Binding identityBindingRoutingKey(Queue identityQueue, TopicExchange identityExchange) {
        return BindingBuilder
                .bind(identityQueue)
                .to(identityExchange)
                .with(AmqpConstants.IDENTITY_BINDING_ROUTING_KEY);
    }

}
