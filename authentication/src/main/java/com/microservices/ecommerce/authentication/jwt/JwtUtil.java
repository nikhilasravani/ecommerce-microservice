package com.microservices.ecommerce.authentication.jwt;

import com.microservices.ecommerce.authentication.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateToken(String username, Role role, UUID userId) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime()+jwtExpiration)))
                .signWith(key())
                .compact();
    }

    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    public String extractUsernameFromToken(String token){
        return extractClaimsFromToken(token).getSubject();
    }

    public String extractRoleFromToken(String token) {
        return extractClaimsFromToken(token).get("role", String.class);
    }

    public boolean validateToken(String token){
        try{
            extractUsernameFromToken(token);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    private UUID extractUserIdFromToken(String token){
        String userId =  extractClaimsFromToken(token).get("userId", String.class);
        return UUID.fromString(userId);
    }
    private Claims extractClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey)key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
