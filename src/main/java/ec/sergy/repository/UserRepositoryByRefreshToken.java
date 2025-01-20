package ec.sergy.repository;

import ec.sergy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepositoryByRefreshToken extends JpaRepository<User, Long> {
    Optional<User> findByRefreshToken(String refreshToken);
}
