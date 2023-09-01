package com.zqc.itineraryweb.controller;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.Sms;
import com.zqc.itineraryweb.entity.SmsDTO;
import com.zqc.itineraryweb.service.SmsService;
import com.zqc.itineraryweb.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping(value = "/sms")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    // 5分钟 ms数
    private static final long EXPIRATION_TIME = 60 * 1000L;

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping(
            value = "/save",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public Result<Sms> addSmsData(
            @RequestParam("phone_number") String phoneNumber,
            @RequestParam("sms_code") String code,
            @RequestParam("biz_id") String bizId,
            @RequestParam("send_time") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime sendTime
    ) {
        if (isPhoneNumberValid(phoneNumber) && isSmsCodeValid(code)) {
            return Result.error("请检查输入");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedCode = passwordEncoder.encode(code);
        String hashedPhoneNumber = passwordEncoder.encode(code);

        Map<String, Object> claims = new HashMap<>();
        claims.put("phone_number", hashedPhoneNumber);
        claims.put("sms_code", hashedCode);
        claims.put("biz_id", bizId);
        String jwt = JwtUtils.generateJwt(claims, EXPIRATION_TIME);

        Sms sms = new Sms();
        sms.setPhoneNumber(phoneNumber);
        sms.setSmsCode(hashedCode);
        sms.setBizId(bizId);
        sms.setSendTime(sendTime);
        sms.setExpire(jwt);

        int byPhoneNumber = smsService.querySmsByPhoneNumber(phoneNumber);
        if (byPhoneNumber == 0) {
            // 不是相同手机号则可以插入新行
            smsService.insertSmsData(sms);
        } else {
            // 直接更新对应手机号所在行信息
            smsService.updateSmsData(sms);
        }
        return Result.success();
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
        if (isPhoneNumberValid(phoneNumber) && isSmsCodeValid(code)) {
            return Result.error("请检查输入");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Sms querySmsData = smsService.querySmsData(phoneNumber, bizId);

        if (querySmsData != null) {
            if (!passwordEncoder.matches(code, querySmsData.getSmsCode())) {
                return Result.error("验证码错误");
            }

            String jwt = querySmsData.getExpire();
            try {
                JwtUtils.parseJwt(jwt);
            } catch (ExpiredJwtException e) {
                return Result.error("验证码已过有效期");
            } catch (JwtException e) {
                logger.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
                return Result.error("JWT解析错误");
            }

            SmsDTO smsDTO = new SmsDTO();
            smsDTO.setStatus(true);
            smsDTO.setPhoneNumber(querySmsData.getPhoneNumber());
            smsDTO.setBizId(querySmsData.getBizId());

            smsService.deleteByPhoneNumber(phoneNumber);
            return Result.success(smsDTO);
        }
        return Result.error("验证码错误");
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        // 定义正则表达式，匹配11位数字
        String regex = "^[0-9]{11}$";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(phoneNumber);
        // 执行匹配并返回结果
        return !matcher.matches();
    }

    private boolean isSmsCodeValid(String smsCode) {
        // 定义正则表达式，匹配数字
        String regex = "^[0-9]$";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(smsCode);
        // 执行匹配并返回结果
        return !matcher.matches();
    }

}