package com.zqc.itineraryweb.controller;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.User;
import com.zqc.itineraryweb.service.UserService;
import com.zqc.itineraryweb.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 15天 ms数 15 * 24 * 60 * 60 * 1000L
    private static final long EXPIRATION_TIME = 60 * 1000L;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public Result<String> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        User loginUser = userService.login(username);
        if (loginUser != null) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            if (passwordEncoder.matches(password, loginUser.getPassword())) {
                UUID uuid = convertBytesToUUID(loginUser.getUserId());
                String timeFormat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String jwtToken = loginUser.getToken();

                if (jwtToken == null || JwtUtils.isJwtExpired(jwtToken)) {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("userId", uuid);
                    claims.put("username", loginUser.getUsername());
                    claims.put("lastLoginAt", timeFormat);

                    jwtToken = JwtUtils.generateJwt(claims, EXPIRATION_TIME);

                    loginUser.setToken(jwtToken);
                    loginUser.setLastLoginAt(LocalDateTime.now());
                    userService.updateByTokenAndLastLoginAt(loginUser);
                }
                return Result.success(jwtToken);
            }
        }
        return Result.error("登录失败，用户名或密码错误");
    }

    @PostMapping(value = "/register")
    public Result<String> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        if (isValidUsername(username) && isValidPassword(password)) {
            int byUsername = userService.getHasByUsername(username);
            if (byUsername > 0) {
                return Result.error("用户名已存在");
            }

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(password);

            byte[] uuidBytes = generateUUIDToBytes();
            User user = new User();
            user.setUserId(uuidBytes);
            user.setUsername(username);
            user.setPassword(hashedPassword);

            userService.register(user);
            return Result.success();
        }
        return Result.error("注册失败，请检查输入");
    }

    @PostMapping(value = "/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            // 清除用户认证信息
            SecurityContextHolder.clearContext();

            // 使会话失效
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 清除用户在服务器端的相关状态信息
            String jwtToken = request.getHeader("Authorization");

            if (jwtToken.isEmpty()) {
                return Result.error("登出失败，请求头token为空");
            }

            try {
                Claims claims = JwtUtils.parseJwt(jwtToken);
                String username = claims.get("username", String.class);
                userService.clearUserToken(username, null);

                return Result.success();
            } catch (ExpiredJwtException e) {
                log.error("令牌已过有效期");
                return Result.error("登出失败");
            } catch (JwtException e) {
                logger.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
                return Result.error("登出失败");
            }

        } catch (Exception e) {
            logger.error("发生了错误: {}, 错误信息为: {}", e, e.getMessage());
            return Result.error("登出失败");
        }
    }

    private byte[] generateUUIDToBytes() {
        UUID uuid = UUID.randomUUID();
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    private UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long mostSigBits = byteBuffer.getLong();
        long leastSigBits = byteBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    private boolean isValidUsername(String username) {
        // 字母、数字、下划线、连字符，长度范围为 3 ~ 16 个字符
        String regex = "^[a-zA-Z0-9_-]{3,16}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean isValidPassword(String password) {
        // 至少 8 个字符，至少包含一个大写，一个小写，一个数字，一个特殊字符
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[-@#$%^&+?;,.=!])(?!.*\\s).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

}
