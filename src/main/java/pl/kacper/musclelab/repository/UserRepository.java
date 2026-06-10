package pl.kacper.musclelab.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.kacper.musclelab.model.Role;
import pl.kacper.musclelab.model.User;

import java.util.Optional;

public interface UserRepository extends
        JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {
    // Optionals
    Optional<User> findUserByUserIdEqualsIgnoreCase(String userId);
    Optional<User> findUserByUsernameEqualsIgnoreCase(String username);
    Optional<User> findUserByEmailEqualsIgnoreCase(String email);
}
