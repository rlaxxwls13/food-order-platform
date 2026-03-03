package nbcamp.food_order_platform.product.repository;

import nbcamp.food_order_platform.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    // 고객/일반 조회: 숨김 제외 + 삭제 제외
    List<Product> findAllByStoreIdAndIsHiddenFalse(UUID storeId);

    Optional<Product> findByIdAndStoreIdAndIsHiddenFalse(UUID productId, UUID storeId);

    // 사장/관리자: 삭제 제외(숨김 포함)
    List<Product> findAllByStoreId(UUID storeId);

    Optional<Product> findByIdAndStoreId(UUID productId, UUID storeId);

    // 사장/관리자: 삭제 포함(숨김 포함)
    @Query(value = "select * from p_product where store_id = :storeId", nativeQuery = true)
    List<Product> findAllByStoreIdIncludingDeleted(@Param("storeId")UUID storeId);

    // 사장/관리자: 삭제된 것만(숨김 포함)
    @Query(value = "select * from p_product where store_id = :storeId and deleted_at is not null", nativeQuery = true)
    List<Product> findAllDeletedByStoreId(@Param("storeId")UUID storeId);

}
