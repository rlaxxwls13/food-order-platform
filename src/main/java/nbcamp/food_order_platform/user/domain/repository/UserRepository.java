package nbcamp.food_order_platform.user.domain.repository;

import nbcamp.food_order_platform.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    User findAllByUserId(Long userId);

    Page<User> findByUsernameContainingAndDeletedAtIsNull(String username, Pageable pageable);
}
