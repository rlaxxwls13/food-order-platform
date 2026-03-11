package nbcamp.food_order_platform.ai.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_ai_description")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiDescription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_log_id", nullable = false, updatable= false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "request_text", nullable = false, length = 100)
    private String requestText;

    @Column(name = "response_text", nullable = false, length = 50)
    private String responseText;

    public AiDescription(UUID productId, String requestText, String responseText) {
        this.productId = productId;
        this.requestText = requestText;
        this.responseText = responseText;

    }
}