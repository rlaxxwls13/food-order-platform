package nbcamp.food_order_platform.store.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Entity
@Table(name = "p_store")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "total_rating_sum", nullable = false)
    private int totalRatingSum;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder
    public Store(Long ownerId, Long regionId, String name) {
        validate(ownerId, regionId, name);
        this.ownerId = ownerId;
        this.regionId = regionId;
        this.name = name;
        this.totalRatingSum = 0;
        this.reviewCount = 0;
    }

    private static void validate(Long ownerId, Long regionId, String name) {
        if(ownerId == null || regionId == null || name == null || name.isBlank())
            throw new IllegalArgumentException("ownerId, regionId, name은 필수입니다.");
        if(name.length() > 255)
            throw new IllegalArgumentException("name(가게명)은 255자 이하여야합니다.");
    }

    public void changeOwner(Long ownerId) {
        if(ownerId == null)
            throw new IllegalArgumentException("ownerId(사장님 아이디)가 비어있습니다.");
        this.ownerId = ownerId;
    }

    public void changeRegion(Long regionId) {
        if(regionId == null)
            throw new IllegalArgumentException("regionId(지역 아이디)가 비어있습니다.");
        this.regionId = regionId;
    }

    public void changeName(String name) {
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("name(가게명)이 비어있습니다.");
        if(name.length() > 255)
            throw new IllegalArgumentException("name(가게명)은 255자 이하여야합니다.");
        this.name = name;
    }

    public void addNewRating(int rating) {
        if(rating < 1 || rating > 5)
            throw new IllegalArgumentException("리뷰 점수는 1에서 5 사이여야 합니다.");
        this.totalRatingSum += rating;
        this.reviewCount += 1;
    }

    public void removeRating(int rating) {
        if (this.reviewCount > 0) {
            this.totalRatingSum -= rating;
            this.reviewCount -= 1;
        }
    }

    public double getRating() {
        if(reviewCount == 0)
            return 0;
        double rating = (double) totalRatingSum / reviewCount;
        return Math.round(rating*10)/10.0;
    }

}
