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

    /**
     * Retrieves all users from the database.
     * This method queries the UserRepository to fetch and return a list of all UserModel entities.
     *
     * @return a list containing all users in the database
     */
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Saves the given UserModel to the database and then publishes an event with the saved user.
     * This method is transactional, ensuring that the event is only published if the user is successfully saved.
     * First, it persists the user using the UserRepository, then it triggers the UserProducer to publish the event.
     *
     * @param userModel the user entity to be saved and published
     * @return the persisted UserModel
     */
    @Transactional
    public UserModel saveAndPublish (UserModel userModel) {
        userModel = userRepository.save(userModel);
        userProducer.publishEvent(userModel);
        return userModel;
    }


}
