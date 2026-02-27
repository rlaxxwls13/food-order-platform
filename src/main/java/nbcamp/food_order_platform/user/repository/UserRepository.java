package nbcamp.food_order_platform.user.repository;

import nbcamp.food_order_platform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
