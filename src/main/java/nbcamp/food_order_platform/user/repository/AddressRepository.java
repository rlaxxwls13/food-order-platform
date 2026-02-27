package nbcamp.food_order_platform.user.repository;

import nbcamp.food_order_platform.user.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
}
