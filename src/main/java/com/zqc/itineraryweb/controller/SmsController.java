package com.zqc.itineraryweb.controller;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.SmsDTO;
import com.zqc.itineraryweb.service.SmsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * 发送验证码
     *
     * @param phoneNumber 国内合法11位手机号
     * @return Result
     */
    @PostMapping(
            value = "/send",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public Result<Object> sendSmsCode(@RequestParam("phone_number") String phoneNumber) {
        return smsService.sendSmsCode(phoneNumber);
    }

    /**
     * 校验验证码
     *
     * @param phoneNumber 国内合法11位手机号
     * @param code        验证码
     * @param bizId       发送验证码的发送回执
     * @return Result
     */
    @PostMapping(
            value = "/check",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public Result<SmsDTO> validateSmsCode(
            @RequestParam("phone_number") String phoneNumber,
            @RequestParam("sms_code") String code,
            @RequestParam("biz_id") String bizId
    ) {
        return smsService.validateSmsCode(phoneNumber, code, bizId);
    }

}