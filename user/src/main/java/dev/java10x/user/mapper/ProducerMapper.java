package dev.java10x.user.mapper;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.ProducerDto;
import dev.java10x.user.enums.EventType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProducerMapper {

    public ProducerDto toProducerDto(UserModel userModel, EventType eventType) {
        var producerDto = new ProducerDto();
        producerDto.setUserId(userModel.getUserId());
        producerDto.setName(userModel.getName());
        producerDto.setEmail(userModel.getEmail());
        producerDto.setEventType(eventType);
        return producerDto;
    }

    public List<ProducerDto> toProducerDtoList(List<UserModel> users, EventType eventType) {
        return users.stream()
                .map(user -> toProducerDto(user, eventType))
                .toList();
    }
}
