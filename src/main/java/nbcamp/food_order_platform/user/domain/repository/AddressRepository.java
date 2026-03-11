package nbcamp.food_order_platform.user.domain.repository;

import nbcamp.food_order_platform.user.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByUser_UserIdAndDeletedAtIsNull(Long userId);

    List<Address> findAllByUser_UserId(Long userId);
}
