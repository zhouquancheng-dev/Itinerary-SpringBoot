package com.zqc.itineraryweb.controllers.user;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录
     *
     * @param username 用户名（国内合法11位手机号码）
     * @param password 密码
     * @return Result
     */
    @PostMapping(value = "/login")
    public Result<Object> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        return userService.login(username, password);
    }

    /**
     * 注册用户
     *
     * @param username 用户名（国内合法11位手机号码）
     * @param password 密码
     * @return Result
     */
    @PostMapping(value = "/register")
    public Result<Object> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirm_password") String confirmPassword
    ) {
        return userService.register(username, password, confirmPassword);
    }

    /**
     * 退出登录
     *
     * @param token jwt
     * @return Result
     */
    @PostMapping(value = "/logout")
    public Result<Object> logout(@RequestHeader("token") String token) {
        return userService.logout(token);
    }

    /**
     * 客户端根据请求头中token验证自动登录
     *
     * @param token jwt
     * @return Result
     */
    @PostMapping(value = "/auto-login")
    public Result<Object> autoLogin(@RequestHeader("token") String token) {
        return userService.autoLogin(token);
    }

    /**
     * 手机登录
     *
     * @param phoneNumber 国内合法11位手机号码
     * @return Result
     */
    @PostMapping(value = "/phone/login-verify")
    public Result<Object> phoneNumberLogin(@RequestParam("phone_number") String phoneNumber) {
        return userService.phoneNumberLogin(phoneNumber);
    }

    /**
     * 检查用户是否注册
     *
     * @param username 用户名（国内合法11位手机号码）
     * @return Result
     */
    @PostMapping(value = "/check-registration")
    public Result<Object> checkRegistration(@RequestParam("username") String username) {
        return userService.checkRegistration(username);
    }

    /**
     * 重设密码
     *
     * @param username 用户名（国内合法11位手机号码）
     * @param newPassword 新密码
     * @param confirmNewPassword 确认新密码
     * @return Result
     */
    @PostMapping(value = "/reset/password")
    public Result<Object> resetPassword(
            @RequestParam("username") String username,
            @RequestParam("new_password") String newPassword,
            @RequestParam("confirm_new_password") String confirmNewPassword
    ) {
        return userService.resetPassword(username, newPassword, confirmNewPassword);
    }
}