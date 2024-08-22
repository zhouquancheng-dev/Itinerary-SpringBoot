package com.zqc.itineraryweb.controllers.login;

import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.login.LoginTokenVerifyRequest;
import com.zqc.itineraryweb.service.LoginService;
import com.zqc.itineraryweb.utils.aliyun.AliYunSmsVerifyClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 极光一键登录验证
     */
    @PostMapping(
            value = "/tokenVerify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Result<?>> loginTokenVerify(
            @RequestBody LoginTokenVerifyRequest request
    ) {
        return loginService.verifyLoginToken(request);
    }

    /**
     * 发送验证码
     *
     * @param phoneNumber  手机号
     * @param codeLength   验证码长度支持4～8位长度
     * @param validTime    验证码有效时长
     * @param sendInterval 时间间隔
     */
    @PostMapping(value = "/sendSmsCode")
    public Result<SendSmsVerifyCodeResponseBody> sendSmsVerifyCode(
            @RequestParam String phoneNumber,
            @RequestParam(defaultValue = "6") long codeLength,
            @RequestParam(defaultValue = "300") long validTime,
            @RequestParam(defaultValue = "60") long sendInterval
    ) {
        if (codeLength < 4 || codeLength > 8) {
            codeLength = 6L;
        }
        return AliYunSmsVerifyClient.sendSmsVerifyCode(phoneNumber, codeLength, validTime, sendInterval);
    }

    /**
     * 核验验证码
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     */
    @PostMapping(value = "/verifyCode")
    public Result<CheckSmsVerifyCodeResponseBody> checkSmsVerifyCode(
            @RequestParam String phoneNumber,
            @RequestParam String verifyCode
    ) {
        return AliYunSmsVerifyClient.checkSmsVerifyCode(phoneNumber, verifyCode);
    }
}