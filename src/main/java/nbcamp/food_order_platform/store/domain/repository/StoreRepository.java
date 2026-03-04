package nbcamp.food_order_platform.store.domain.repository;

import nbcamp.food_order_platform.store.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    @Query(value = "SELECT * FROM p_store WHERE store_id = :id", nativeQuery = true)
    Optional<Store> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query(value = "SELECT * FROM p_store", nativeQuery = true)
    List<Store> findAllIncludingDeleted();
}
