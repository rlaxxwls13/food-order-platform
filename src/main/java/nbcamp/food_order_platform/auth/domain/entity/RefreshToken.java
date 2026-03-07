package nbcamp.food_order_platform.auth.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiryDate;

    public RefreshToken(Long userId, String token, LocalDateTime expiryDate){
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired(){
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
