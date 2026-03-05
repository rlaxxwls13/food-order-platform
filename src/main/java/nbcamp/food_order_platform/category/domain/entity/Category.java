package nbcamp.food_order_platform.category.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "category")
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    public static Category create(String name) {
        Category category = new Category();
        category.name = name;
        return category;
    }
}
