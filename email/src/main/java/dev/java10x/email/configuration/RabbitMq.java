package dev.java10x.email.configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    private final String queueName = "email-queue";
    private final String usersListQueueName = "users-list-queue";
    private final String simulatedDelayQueueName = "simulated-delay-queue";

    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue usersListQueue() {
        return new Queue(usersListQueueName, true);
    }

    @Bean
    public Queue simulatedDelayQueue() {
        return new Queue(simulatedDelayQueueName, true);
    }

   @Bean
   public Jackson2JsonMessageConverter messageConverter() {
       ObjectMapper objectMapper = new ObjectMapper();
       return new Jackson2JsonMessageConverter(objectMapper);
   }
}
