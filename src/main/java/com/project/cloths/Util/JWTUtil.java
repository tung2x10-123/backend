package com.project.cloths.Util;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

@Component
public class JWTUtil {



//    private Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private String secretKey = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456"; // Key bí mật
    private long expiration = 86400000; // 1 ngày

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS256, secretKey)
                .signWith(SignatureAlgorithm.HS256, Keys.hmacShaKeyFor(secretKey.getBytes()))
//                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).parseClaimsJws(token).getBody().getSubject();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
