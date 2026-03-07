package nbcamp.food_order_platform.store.domain.repository;

import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRegionRepository extends JpaRepository<StoreRegion, UUID> {
    Optional<StoreRegion> findByStore(Store store);
    Optional<StoreRegion> findByStoreId(UUID storeId);
}
