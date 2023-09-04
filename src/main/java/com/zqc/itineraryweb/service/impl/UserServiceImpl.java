package com.zqc.itineraryweb.service.impl;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.User;
import com.zqc.itineraryweb.dao.UserRepository;
import com.zqc.itineraryweb.service.RSAKeyService;
import com.zqc.itineraryweb.service.UserService;
import com.zqc.itineraryweb.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static com.zqc.itineraryweb.utils.UUIDUtils.*;
import static com.zqc.itineraryweb.utils.ValidationUtils.isValidPassword;
import static com.zqc.itineraryweb.utils.ValidationUtils.isValidUsername;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    // 5天过期 5 * 24 * 60 * 60 * 1000L
    private static final long EXPIRATION_TIME = 5 * 24 * 60 * 60 * 1000L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RSAKeyService rsaKeyService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RSAKeyService rsaKeyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rsaKeyService = rsaKeyService;
    }

    @Override
    public Result<Object> userLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return Result.error("登录失败，请检查输入");
        }

        User user = userRepository.findUserByUsername(username);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                UUID uuid = convertBytesToUUID(user.getUserId());
                String jwtToken = user.getToken();

                if (jwtToken == null || JwtUtils.isJwtExpired(jwtToken)) {
                    // 非对称加密密码
                    String encryptedPassword = rsaKeyService.encryptPassword(password);

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("userId", uuid);
                    claims.put("username", user.getUsername());
                    claims.put("encryptedPassword", encryptedPassword);

                    jwtToken = JwtUtils.generateJwt(claims, EXPIRATION_TIME);
                }

                // 更新用户的jwt令牌和最后登录时间
                userRepository.updateTokenAndLastLoginAtByUserIdAndUsername(
                        jwtToken,
                        LocalDateTime.now(),
                        user.getUserId(),
                        username
                );

                return Result.success(jwtToken);
            }
        }
        return Result.error("登录失败，用户名或密码错误");
    }

    @Override
    public Result<Object> autoLogin(String token) {
        if (token.isEmpty()) {
            return Result.error("请求头token为空，请重新登录");
        }

        try {
            Claims claims = JwtUtils.parseJwt(token);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            String encryptedPassword = claims.get("encryptedPassword", String.class);

            // 使用私钥解密密码
            String decryptedPassword = rsaKeyService.decryptPassword(encryptedPassword);

            byte[] userIdBytes = convertUUIDToBytes(UUID.fromString(userId));
            String hashedPassword = userRepository.findUserByUserIdAndUsername(userIdBytes, username);
            if (hashedPassword != null) {
                if (passwordEncoder.matches(decryptedPassword, hashedPassword)) {
                    // 密码验证成功
                    return Result.success("自动登录成功");
                }
            }
            return Result.error("自动登录失败，用户名或密码不匹配");
        } catch (ExpiredJwtException e) {
            LOGGER.error("自动登录失败，令牌已过有效期");
            return Result.error("登录已过有效期，请重新登录");
        } catch (JwtException e) {
            LOGGER.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
            return Result.error("自动登录失败");
        } catch (Exception e) {
            LOGGER.error("自动登录时发生错误: {}, 错误信息为: {}", e, e.getMessage());
            return Result.error("自动登录失败");
        }
    }

    @Override
    public Result<Object> registerUser(String username, String password) {
        if (!isValidUsername(username) && !isValidPassword(password)) {
            return Result.error("注册失败，请检查输入");
        }

        if (userRepository.existsByUsername(username)) {
            return Result.error("用户名已存在");
        }

        // 哈希加密密码
        String hashedPassword = passwordEncoder.encode(password);

        byte[] uuidBytes = generateUUIDToBytes();
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUserId(uuidBytes);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setLastLoginAt(now);
        user.setCreatedAt(now);

        userRepository.save(user);
        return Result.success();
    }

    @Override
    public Result<Object> logout(String token) {
        if (token.isEmpty()) {
            return Result.error("登出失败，请求头token为空");
        }

        try {
            Claims claims = JwtUtils.parseJwt(token);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);

            byte[] userIdBytes = convertUUIDToBytes(UUID.fromString(userId));
            // 将数据库token置为空
            userRepository.updateTokenByUserIdAndUsername(null, userIdBytes, username);
            return Result.success();
        } catch (ExpiredJwtException e) {
            LOGGER.error("令牌已过有效期");
            return Result.error("登出失败");
        } catch (JwtException e) {
            LOGGER.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
            return Result.error("登出失败");
        }
    }

}