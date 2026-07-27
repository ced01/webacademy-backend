package fr.webskills.academy.security;

import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generate(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("uid", user.getId().toString());
        return build(claims, user.getEmail() != null ? user.getEmail() : user.getId().toString());
    }

    public String generate(AccessCode accessCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "LEARNER");
        claims.put("accessCodeId", accessCode.getId().toString());
        claims.put("accessCodeLabel", accessCode.getLabel());
        if (accessCode.getWelcomeMessage() != null) {
            claims.put("welcomeMessage", accessCode.getWelcomeMessage());
        }
        if (accessCode.getRecommendedPath() != null) {
            claims.put("recommendedPath", accessCode.getRecommendedPath());
        }
        return build(claims, "access-code:" + accessCode.getId());
    }

    private String build(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims claims(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
    }

    public boolean valid(String token) {
        try {
            return claims(token).getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Hex.decode(secret));
    }
}
