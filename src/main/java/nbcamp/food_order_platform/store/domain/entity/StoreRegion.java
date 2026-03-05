package nbcamp.food_order_platform.store.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_store_region")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreRegion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "region_id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, unique = true, updatable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_code", nullable = false)
    private RegionCode regionCode;

    @Column(name = "detail", nullable = false, length = 255)
    private String detail;

    public static StoreRegion create(Store store, RegionCode regionCode, String detail) {
        StoreRegion sr = new StoreRegion();
        sr.store = store;
        sr.regionCode = regionCode;
        sr.detail = detail;
        return sr;
    }

    public void change(RegionCode regionCode, String addressDetail) {
        this.regionCode = regionCode;
        this.detail = addressDetail;
    }
}
