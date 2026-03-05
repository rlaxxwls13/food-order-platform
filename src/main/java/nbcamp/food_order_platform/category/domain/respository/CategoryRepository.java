package nbcamp.food_order_platform.category.domain.respository;

import nbcamp.food_order_platform.category.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
