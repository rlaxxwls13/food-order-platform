package nbcamp.food_order_platform.regionCode.domain.repository;

import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCodeRepository extends JpaRepository<RegionCode, Long> {
}
