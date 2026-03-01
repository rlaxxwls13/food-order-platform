package nbcamp.food_order_platform.user.domain.repository;

import nbcamp.food_order_platform.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
