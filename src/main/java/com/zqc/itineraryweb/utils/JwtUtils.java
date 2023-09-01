package com.zqc.itineraryweb.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static String secretKey;

    @Value("${jwt.accessKeyId}" + "${jwt.accessKeySecret}")
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
                .setHeaderParam(JwsHeader.KEY_ID, "jwtHeaderKey")
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param jwt jwt令牌
     */
    public static Claims parseJwt(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public static boolean isJwtExpired(String jwtToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("JWT过期");
            return true;
        } catch (JwtException e) {
            logger.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
            return true;
        }
        return false;
    }

}
