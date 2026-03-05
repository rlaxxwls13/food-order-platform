package nbcamp.food_order_platform.store.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.category.domain.entity.Category;
import nbcamp.food_order_platform.global.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Entity
@Table(name = "p_store_category")
public class StoreCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_category_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private Category category;

    public static StoreCategory create(Store store, Category category) {
        if (store == null || category == null)
            throw new IllegalArgumentException("store/category는 필수입니다.");

        StoreCategory sc = new StoreCategory();
        sc.store = store;
        sc.category = category;
        return sc;
    }
}
