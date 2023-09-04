package com.zqc.itineraryweb.controller;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Result<String>
     */
    @PostMapping(value = "/login")
    public Result<Object> userLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        return userService.userLogin(username, password);
    }

    /**
     * 客户端根据请求头中token自动登录
     *
     * @param token jwt
     * @return Result<String>
     */
    @PostMapping("/auto-login")
    public Result<Object> autoLogin(@RequestHeader("Authorization") String token) {
        return userService.autoLogin(token);
    }

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @return Result<String>
     */
    @PostMapping(value = "/register")
    public Result<Object> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        return userService.registerUser(username, password);
    }

    /**
     * 登出
     *
     * @param request 请求头
     * @return Result<String>
     */
    @PostMapping(value = "/logout")
    public Result<Object> logout(HttpServletRequest request) {
        try {
            // 清除用户认证信息
            SecurityContextHolder.clearContext();

            // 使会话失效
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 清除用户在服务器端的相关状态信息
            String token = request.getHeader("Authorization");

            return userService.logout(token);
        } catch (Exception e) {
            LOGGER.error("发生了错误: {}, 错误信息为: {}", e, e.getMessage());
            return Result.error("登出失败");
        }
    }
}