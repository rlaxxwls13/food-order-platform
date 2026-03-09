package nbcamp.food_order_platform.regionCode.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_region_code")
public class RegionCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "region_code", nullable = false, updatable = false)
    private UUID code;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public static RegionCode create(String regionName, boolean active) {
        RegionCode regionCode = new RegionCode();
        regionCode.regionName = regionName;
        regionCode.active = active;
        return regionCode;
    }
}
