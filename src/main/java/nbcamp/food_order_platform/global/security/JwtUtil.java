package nbcamp.food_order_platform.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.user.domain.entity.Role;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username,long userId, Role role){
        return createToken(username, userId, role , jwtProperties.getAccess().getExpiration(), "access");
    }

    public String generateRefreshToken(String username, long userId, Role role){
        return createToken(username, userId, role , jwtProperties.getRefresh().getExpiration(), "refresh");
    }

    private String createToken(String username, long userId, Role role , long expiration, String type){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token){
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token){
        return extractClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token){
        return extractClaims(token).get("role", String.class);
    }

    public String extractType(String token){
        return extractClaims(token).get("type", String.class);
    }

    public boolean validateToken(String token){
        try {
            extractClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    private Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
