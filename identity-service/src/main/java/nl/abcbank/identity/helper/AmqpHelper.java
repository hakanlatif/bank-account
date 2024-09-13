package nl.abcbank.identity.helper;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.JAXBException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmqpHelper {

    public static <T> void publishMessage(RabbitTemplate rabbitTemplate, @Nonnull T data, @Nonnull String exchangeName,
                                          @Nonnull String routingKey) throws JAXBException {
        try {
            byte[] message = XmlHelper.unmarshal(data);
            rabbitTemplate.send(exchangeName, routingKey, new Message(message));
        } catch (JAXBException e) {
            log.error("Unable to parse xml for : {}", data.getClass().getName(), e);
            throw e;
        } catch (AmqpException e) {
            log.error("Amqp error sending message to exchange: {} with routingKey: {}", exchangeName, routingKey, e);
            throw e;
            // This nested catch block ensures that exceptions including (unexpected) unchecked exceptions that might
            // be triggered by RabbitMQ library handled properly to transfer all failed messages to DLQ to not lose message
        } catch (Exception e) {
            log.error("Unable to send message to exchange: {} with routingKey: {}", exchangeName, routingKey, e);
            throw e;
        }
    }

}
