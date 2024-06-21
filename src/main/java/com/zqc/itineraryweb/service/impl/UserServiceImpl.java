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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static com.zqc.itineraryweb.utils.UUIDUtils.*;
import static com.zqc.itineraryweb.utils.ValidationUtils.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // 过期时间
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RSAKeyService rsaKeyService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RSAKeyService rsaKeyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rsaKeyService = rsaKeyService;
    }

    @Override
    public Result<Object> login(String username, String password) {
        if (!isValidUsername(username) || !isValidPassword(password)) {
            return Result.error("登录失败，用户名或密码错误");
        }

        User user = userRepository.findUserByUsername(username);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                UUID uuid = convertBytesToUUID(user.getUserId());
                // 非对称加密密码
                String encryptedPassword = rsaKeyService.encryptPassword(password);

                Map<String, Object> claims = new HashMap<>();
                claims.put("userId", uuid);
                claims.put("username", username);
                claims.put("encryptedPassword", encryptedPassword);
                String jwt = JwtUtils.generateJwt(claims, EXPIRATION_TIME);

                // 更新用户的最后登录时间
                int updatedRows = userRepository.updateLastLoginAtByUserIdAndUsername(
                        LocalDateTime.now(),
                        user.getUserId(),
                        user.getUsername()
                );
                if (updatedRows > 0) {
                    return Result.success(jwt);
                }
            }
        }
        return Result.error("登录失败，用户名或密码错误");
    }

    @Override
    public Result<Object> register(String username, String password, String confirmPassword) {
        if (!isValidUsername(username) || !isValidPassword(password) || !password.equals(confirmPassword)) {
            return Result.error("注册失败，请检查输入");
        }

        if (userRepository.existsByUsername(username)) {
            return Result.error("当前用户名已注册");
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
        return Result.success("注册成功");
    }

    @Override
    public Result<Object> logout(String token) {
        if (token.isEmpty()) {
            return Result.error("退出登录失败，请求头Token为空");
        }
        try {
            Claims claims = JwtUtils.parseJwt(token);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            String encryptedPassword = claims.get("encryptedPassword", String.class);

            String decryptedPassword = rsaKeyService.decryptPassword(encryptedPassword);
            byte[] userIdBytes = convertUUIDToBytes(UUID.fromString(userId));

            User user = userRepository.findUserByUserIdAndUsername(userIdBytes, username);
            if (user != null) {
                if (passwordEncoder.matches(decryptedPassword, user.getPassword())) {
                    return Result.success("退出登录成功");
                }
            }
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过有效期");
            return Result.success();
        } catch (JwtException e) {
            logger.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
        }
        return Result.error("退出登录失败");
    }

    @Override
    public Result<Object> autoLogin(String token) {
        if (token.isEmpty()) {
            return Result.error("自动登录失败，请求头Token为空");
        }

        try {
            Claims claims = JwtUtils.parseJwt(token);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            String encryptedPassword = claims.get("encryptedPassword", String.class);

            String decryptedPassword = rsaKeyService.decryptPassword(encryptedPassword);
            byte[] userIdBytes = convertUUIDToBytes(UUID.fromString(userId));

            User user = userRepository.findUserByUserIdAndUsername(userIdBytes, username);
            if (user != null) {
                if (passwordEncoder.matches(decryptedPassword, user.getPassword())) {
                    return Result.success("自动登录成功");
                } else {
                    return Result.error("自动登录失败，用户名或密码错误");
                }
            } else {
                return Result.error("自动登录失败，用户id错误");
            }
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过有效期");
            return Result.error("登录已过有效期，请重新登录");
        } catch (JwtException e) {
            logger.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
        } catch (Exception e) {
            logger.error("发生错误: {}, 错误信息为: {}", e, e.getMessage());
        }
        return Result.error("自动登录失败");
    }

    @Override
    public Result<Object> phoneNumberLogin(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            return Result.error("请检查手机号码");
        }
        if (!userRepository.existsByUsername(phoneNumber)) {
            // 生成随机密码
            String randomPassword = generateRandomPassword();
            // 哈希加密密码
            String hashedPassword = passwordEncoder.encode(randomPassword);
            byte[] uuidBytes = generateUUIDToBytes();
            LocalDateTime now = LocalDateTime.now();

            User user = new User();
            user.setUserId(uuidBytes);
            user.setUsername(phoneNumber);
            user.setPassword(hashedPassword);
            user.setLastLoginAt(now);
            user.setCreatedAt(now);

            userRepository.save(user);
        }
        return Result.success("手机登录成功");
    }

    @Override
    public Result<Object> checkRegistration(String username) {
        if (!isValidUsername(username)) {
            return Result.error("请检查输入账号");
        }

        if (!userRepository.existsByUsername(username)) {
            return Result.error("当前用户名未注册");
        } else {
            return Result.success();
        }
    }

    @Override
    public Result<Object> resetPassword(String username, String newPassword, String confirmNewPassword) {
        if (!isValidUsername(username)) {
            return Result.error("重设密码失败，请检查用户名");
        }
        User user = userRepository.findUserByUsername(username);
        if (user != null) {
            if (!isValidPassword(newPassword) || !newPassword.equals(confirmNewPassword)) {
                return Result.error("重设密码失败，请检查密码");
            }
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return Result.error("重设密码失败，不能与旧密码相同");
            }
            byte[] uuidBytes = generateUUIDToBytes();
            String hashedPassword = passwordEncoder.encode(newPassword);
            int updatedRows = userRepository.updateUserIdAndPasswordByUsername(uuidBytes, hashedPassword, username);
            if (updatedRows > 0) {
                return Result.success("重设密码成功");
            }
        }
        return Result.error("重设密码失败");
    }

    private String generateRandomPassword() {
        // 生成一个包含大小写字母和数字的随机字符串作为密码
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8); // 密码长度为8，可以根据需要调整
        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(chars.length());
            password.append(chars.charAt(randomIndex));
        }
        return password.toString();
    }
}