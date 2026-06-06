package org.jas.ksinxapp.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Bytes;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret:}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    public void init(){
        if (jwtSecret == null || jwtSecret.isBlank()) {
            // No secret configured (e.g. local dev): generate an ephemeral key so the app still
            // boots. Tokens are invalidated on every restart — set JWT_SECRET in production.
            this.key = Jwts.SIG.HS256.key().build();
            log.warn("jwt.secret is not set — generated an ephemeral dev key. " +
                    "Set the JWT_SECRET env var to a stable base64 256-bit key in production.");
        } else {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }


    public String generateToken(User.Role role, String email){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("email", email);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (jwtExpiration * 1000)))
                .signWith(key)
                .compact();
    }


    //to get the email from jwt token
    public String getUserFromToken(String token){
        return Jwts.parser().
                verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //validate
    public boolean validateJwtToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch(Exception e){
            log.error("Jwt validation error: {}", e.getMessage());
        }
        return false;
    }

    
}
