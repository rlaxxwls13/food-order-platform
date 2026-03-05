package nbcamp.food_order_platform.store.domain.repository;

import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {

    List<StoreCategory> findAllByStoreId(UUID storeId);

    boolean existsByStoreIdAndCategory_Id(UUID storeId, Long categoryId);

    long countByStoreId(UUID storeId);
}
