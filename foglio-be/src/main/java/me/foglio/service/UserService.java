package me.foglio.service;

import me.foglio.model.User;
import me.foglio.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String name) {
        User u = new User();
        u.setName(name);
        u.setApiKey(UUID.randomUUID());
        return userRepository.save(u);
    }

    public Optional<User> findByApiKey(UUID key) {
        return userRepository.findByApiKey(key);
    }

}
