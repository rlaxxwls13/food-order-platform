package nbcamp.food_order_platform.ai.domain.repository;

import nbcamp.food_order_platform.ai.domain.entity.AiDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiDescriptionRepository extends JpaRepository<AiDescription, UUID> {
    Page<AiDescription> findByProductId(UUID productId, Pageable pageable);
}
