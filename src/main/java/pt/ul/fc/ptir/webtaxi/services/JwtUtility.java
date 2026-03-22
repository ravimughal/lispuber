package pt.ul.fc.ptir.webtaxi.services;

import java.sql.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtility {

    /*
    * Hash Generator: https://www.akto.io/tools/hmac-sha-256-hash-generator
    * 
    * Text: "Happy World 2024"
    * Key: "Key2024"
    * Algorithm: SHA-256 Hash (in Hex)
    */
    @Value("${com.secapp.jwt.secret}")
    private String SECRET_KEY;
    
    public String generateToken(Map<String, String> extraClaims, String userName, long expireInterval) {
    return Jwts
                .builder()
                .claims().add(extraClaims)
                .and()
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireInterval))
                .signWith(getSignInKey())
                .compact();
    }
    
    public String getUserName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }
    
    public boolean isTokenExpired(String token) {
    Claims claims = extractAllClaims(token);
    return claims.getExpiration().before(new java.util.Date());
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        }
    
    private SecretKey getSignInKey() {
    byte[] keyBytes = SECRET_KEY.getBytes();
            return Keys.hmacShaKeyFor(keyBytes);
        }
}