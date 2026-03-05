package nbcamp.food_order_platform.store.domain.repository;

import nbcamp.food_order_platform.store.domain.entity.StoreRegion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRegionRepository extends JpaRepository<StoreRegion, Long> {
}
