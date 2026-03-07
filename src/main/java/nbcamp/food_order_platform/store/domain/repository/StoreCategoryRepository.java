package nbcamp.food_order_platform.store.domain.repository;

import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, UUID> {

    List<StoreCategory> findAllByStoreId(UUID storeId);

    List<StoreCategory> findAllByStore(Store store);

    boolean existsByStoreIdAndCategory_Id(UUID storeId, UUID categoryId);

    long countByStoreId(UUID storeId);
}
