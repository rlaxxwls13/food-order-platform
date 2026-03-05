package nbcamp.food_order_platform.regionCode.domain.repository;

import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionCodeRepository extends JpaRepository<RegionCode, UUID> {
}
