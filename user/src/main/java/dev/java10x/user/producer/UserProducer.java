package dev.java10x.user.producer;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.UserEventDto;
import dev.java10x.user.enums.EventType;
import dev.java10x.user.mapper.UserEventMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserProducer {

    private static final String DEFAULT_EXCHANGE = "";

    private final RabbitTemplate rabbitTemplate;
    private final UserEventMapper userEventMapper;
    private final String emailNotificationQueue;
    private final String simulatedDelayQueue;

    public UserProducer(
            RabbitTemplate rabbitTemplate,
            UserEventMapper userEventMapper,
            @Value("${app.rabbitmq.queues.email-notification}") String emailNotificationQueue,
            @Value("${app.rabbitmq.queues.simulated-delay}") String simulatedDelayQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.userEventMapper = userEventMapper;
        this.emailNotificationQueue = emailNotificationQueue;
        this.simulatedDelayQueue = simulatedDelayQueue;
    }

    public void publishUserCreatedEvent(UserModel userModel) {
        UserEventDto event = userEventMapper.toUserEventDto(userModel, EventType.USER_CREATED);
        rabbitTemplate.convertAndSend(DEFAULT_EXCHANGE, emailNotificationQueue, event);
    }

    public void publishSimulatedDelayRequestedEvents(List<UserModel> users) {
        users.forEach(user -> {
            UserEventDto event = userEventMapper.toUserEventDto(user, EventType.SIMULATED_DELAY_REQUESTED);
            rabbitTemplate.convertAndSend(DEFAULT_EXCHANGE, simulatedDelayQueue, event);
        });
    }
}
