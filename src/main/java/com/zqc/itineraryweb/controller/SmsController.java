package com.zqc.itineraryweb.controller;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.SmsDTO;
import com.zqc.itineraryweb.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping(value = "/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping(
            value = "/save",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public Result<Object> saveSmsData(
            @RequestParam("phone_number") String phoneNumber,
            @RequestParam("sms_code") String code,
            @RequestParam("biz_id") String bizId,
            @RequestParam("send_time") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime sendTime
    ) {
        return smsService.saveSmsData(phoneNumber, code, bizId, sendTime);
    }

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