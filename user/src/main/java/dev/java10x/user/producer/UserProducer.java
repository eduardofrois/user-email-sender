package dev.java10x.user.producer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.ProducerDto;
import dev.java10x.user.enums.EventType;
import dev.java10x.user.mapper.ProducerMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserProducer {

    final RabbitTemplate rabbitTemplate;
    final ProducerMapper producerMapper;
    final ObjectMapper objectMapper;

    public UserProducer(RabbitTemplate rabbitTemplate, ProducerMapper producerMapper, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.producerMapper = producerMapper;
        this.objectMapper = objectMapper;
    }

    private String routingEmailKey = "email-queue";
    private String simulatedDelayKey = "simulated-delay-queue";

    public void sendEmailEvent(UserModel userModel) {
        ProducerDto producerDto = producerMapper.toProducerDto(userModel, EventType.USER_CREATED);
        rabbitTemplate.convertAndSend("", routingEmailKey, toJson(producerDto));
    }

    public void sendSimulatedDelayEvent(List<UserModel> users) {
        users.forEach(user -> {
            ProducerDto producerDto = producerMapper.toProducerDto(user, EventType.SIMULATED_DELAY_REQUESTED);
            rabbitTemplate.convertAndSend("", simulatedDelayKey, toJson(producerDto));
        });
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Erro ao converter payload para JSON", exception);
        }
    }

}
    
