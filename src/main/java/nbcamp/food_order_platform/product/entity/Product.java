package nbcamp.food_order_platform.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Entity
@Table(name = "p_product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private int price;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Product(UUID storeId, String name, String description, int quantity, int price) {
        validate(storeId, name, description, quantity, price);
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.isHidden = false;
    }

    private static void validate(UUID storeId, String name, String description, int quantity, int price) {
        if(storeId == null || name == null || name.isBlank() || description == null || description.isBlank())
            throw new IllegalArgumentException("storeId(가게 아이디), name(상품 이름), description(상품 설명)은 필수입니다.");
        if(name.length() > 50)
            throw new IllegalArgumentException("name(상품 이름)은 50자 이내여야합니다.");
        if(description.length() > 100)
            throw new IllegalArgumentException("description(상품 설명)은 100자 이내여야합니다.");
        if(quantity < 0)
            throw new IllegalArgumentException("quantity(상품 수량)은 음수일 수 없습니다.");
        if(price < 0)
            throw new IllegalArgumentException("price(상품 가격)은 음수일 수 없습니다.");
    }

    public void hide() {
        this.isHidden = true;
    }
    public void unhide() {
        this.isHidden = false;
    }

    public void changeName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name은 필수입니다.");
        if (name.length() > 50)
            throw new IllegalArgumentException("name은 50자 이하여야 합니다.");
        this.name = name;
    }

    public void changeDescription(String description) {
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("description은 필수입니다.");
        if (description.length() > 100)
            throw new IllegalArgumentException("description은 100자 이하여야 합니다.");
        this.description = description;
    }

    public void changeQuantity(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("quantity는 0 이상이어야 합니다.");
        this.quantity = quantity;
    }

    public void changePrice(int price) {
        if (price < 0)
            throw new IllegalArgumentException("price는 0 이상이어야 합니다.");
        this.price = price;
    }

    public void decreaseStock(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("감소 수량은 1 이상이어야 합니다.");
        if (this.quantity < amount)
            throw new IllegalArgumentException("재고가 부족합니다.");
        this.quantity -= amount;
    }

    public void increaseStock(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("증가 수량은 1 이상이어야 합니다.");
        this.quantity += amount;
    }
}
