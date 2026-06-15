package dev.java10x.user.service;
import dev.java10x.user.domain.UserModel;
import dev.java10x.user.producer.UserProducer;
import dev.java10x.user.repositorie.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final UserProducer userProducer;

    public UserService(UserRepository userRepository, UserProducer userProducer) {
        this.userRepository = userRepository;
        this.userProducer = userProducer;
    }

    public List<UserModel> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        userProducer.sendListUsersEvent(users);
        return users;
    }

    @Transactional
    public UserModel saveAndPublish (UserModel userModel) {
        userModel = userRepository.save(userModel);
        userProducer.sendEmailEvent(userModel);
        return userModel;
    }

    @Transactional
    public List<UserModel> saveAll(List<UserModel> users) {
        List<UserModel> usersCreadet = userRepository.saveAll(users);
        userProducer.sendSimulatedDelayEvent(usersCreadet);
        return usersCreadet;
    }

}
