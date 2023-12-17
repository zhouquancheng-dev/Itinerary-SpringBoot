package com.zqc.itineraryweb.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private static String secretKey;

    @Value("${jwt.accessKey}")
    public void setSecretKey(String secretKey) {
        JwtUtils.secretKey = secretKey;
    }

    /**
     * 生成JWT令牌
     *
     * @param claims payload内容
     * @return jwt令牌
     */
    public static String generateJwt(Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .header().add("kid", "jwtHeaderKey").and()
                .claims().add(claims).and()
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), Jwts.SIG.HS256)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param jwt jwt令牌
     */
    public static Claims parseJwt(String jwt) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

}