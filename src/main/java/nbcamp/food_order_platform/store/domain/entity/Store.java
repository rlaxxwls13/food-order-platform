package nbcamp.food_order_platform.store.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.category.domain.entity.Category;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
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

    @OneToOne(
            mappedBy = "store",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            optional = false
    )
    private StoreRegion storeRegion;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<StoreCategory> storeCategories = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "total_rating_sum", nullable = false)
    private int totalRatingSum;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Store(Long ownerId, String name, RegionCode regionCode, String addressDetail, List<Category> categories) {
        validate(ownerId, regionCode, addressDetail, name);
        this.ownerId = ownerId;
        this.storeRegion = StoreRegion.create(this, regionCode, addressDetail);
        this.storeCategories.addAll(makeStoreCategoryFromCategory(this, categories));
        this.name = name;
        this.totalRatingSum = 0;
        this.reviewCount = 0;
    }

    public void softDelete(Long userId) {
        if (storeRegion != null) {
            storeRegion.softDelete(userId);
        }
        for (StoreCategory sc : storeCategories) {
            sc.softDelete(userId);
        }
        super.softDelete(userId);
    }

    private static void validate(Long ownerId, RegionCode regionCode, String addressDetail, String name) {
        if(ownerId == null || regionCode == null || name == null || name.isBlank() || addressDetail == null || addressDetail.isBlank())
            throw new IllegalArgumentException("ownerId, regionCode, addressDetail, name은 필수입니다.");
        if(name.length() > 255)
            throw new IllegalArgumentException("name(가게명)은 255자 이하여야합니다.");
    }

    private List<StoreCategory> makeStoreCategoryFromCategory(Store store, List<Category> categories){
        if(categories == null || categories.isEmpty())
            return List.of();

        List<StoreCategory> storeCategoryList = new ArrayList<>();
        for(Category category : categories) {
            storeCategoryList.add(StoreCategory.create(store, category));
        }
        return storeCategoryList;
    }

    public void changeOwner(Long ownerId) {
        if(ownerId == null)
            throw new IllegalArgumentException("ownerId(사장님 아이디)가 비어있습니다.");
        this.ownerId = ownerId;
    }

    public void changeRegion(RegionCode regionCode, String addressDetail) {
        if(regionCode == null || addressDetail == null || addressDetail.isBlank())
            throw new IllegalArgumentException("regionCode(지역) 또는 addressDetail(상세 주소)이 비어있습니다.");
        this.storeRegion.change(regionCode, addressDetail);
    }

    public void addCategory(Category category) {
        if (category == null)
            throw new IllegalArgumentException("category가 비어있습니다.");
        boolean exists = this.storeCategories.stream()
                .anyMatch(sc -> sc.getCategory().getId().equals(category.getId()));
        if (exists) return;

        this.storeCategories.add(StoreCategory.create(this, category));
    }

    public void removeCategory(Category category, Long userId) {
        if (category == null)
            throw new IllegalArgumentException("category가 비어있습니다.");
        if (userId == null)
            throw new IllegalArgumentException("userId가 비어있습니다.");

        this.storeCategories.stream()
                .filter(sc -> sc.getCategory().getId().equals(category.getId()))
                .forEach(sc -> sc.softDelete(userId));
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

    public double getAverageRating() {
        if(reviewCount == 0)
            return 0;
        double rating = (double) totalRatingSum / reviewCount;
        return Math.round(rating*10)/10.0;
    }

}
