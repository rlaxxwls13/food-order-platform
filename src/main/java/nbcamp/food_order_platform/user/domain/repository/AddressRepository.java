package nbcamp.food_order_platform.user.domain.repository;

import nbcamp.food_order_platform.user.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
