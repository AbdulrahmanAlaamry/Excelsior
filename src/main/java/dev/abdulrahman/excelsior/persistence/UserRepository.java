package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.business.user.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @NonNull Optional<User> findById(@NonNull Long id);
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
