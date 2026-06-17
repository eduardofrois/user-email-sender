package dev.java10x.user.mapper;

import dev.java10x.user.domain.UserModel;
import dev.java10x.user.dto.UserEventDto;
import dev.java10x.user.enums.EventType;
import org.springframework.stereotype.Component;

@Component
public class UserEventMapper {

    public UserEventDto toUserEventDto(UserModel userModel, EventType eventType) {
        var userEventDto = new UserEventDto();
        userEventDto.setUserId(userModel.getUserId());
        userEventDto.setName(userModel.getName());
        userEventDto.setEmail(userModel.getEmail());
        userEventDto.setEventType(eventType);
        return userEventDto;
    }
}
