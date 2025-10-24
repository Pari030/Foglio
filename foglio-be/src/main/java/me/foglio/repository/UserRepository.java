package me.foglio.repository;

import me.foglio.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByApiKey(UUID apiKey);
}
