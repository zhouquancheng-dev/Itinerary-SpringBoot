package com.zqc.itineraryweb.service.impl;

import com.zqc.itineraryweb.dao.SmsRepository;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.Sms;
import com.zqc.itineraryweb.entity.SmsDTO;
import com.zqc.itineraryweb.service.SmsService;
import com.zqc.itineraryweb.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.zqc.itineraryweb.utils.MaskUtils.maskPhoneNumber;
import static com.zqc.itineraryweb.utils.ValidationUtils.isValidPhoneNumber;
import static com.zqc.itineraryweb.utils.ValidationUtils.isValidSmsCode;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);

    // 5分钟 5 * 60 * 1000L
    private static final long EXPIRATION_TIME = 5 * 60 * 1000L;

    private final SmsRepository smsRepository;
    private final PasswordEncoder passwordEncoder;

    public SmsServiceImpl(SmsRepository smsRepository, PasswordEncoder passwordEncoder) {
        this.smsRepository = smsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Result<Object> saveSmsData(
            String phoneNumber,
            String code,
            String bizId,
            LocalDateTime sendTime
    ) {
        if (!isValidPhoneNumber(phoneNumber) && !isValidSmsCode(code)) {
            return Result.error("保存失败，请检查输入");
        }

        // 哈希加密手机号、验证码
        String hashedPhoneNumber = passwordEncoder.encode(code);
        String hashedCode = passwordEncoder.encode(code);

        // 创建Jwt令牌并添加令牌负载
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
        sms.setToken(jwt);
        sms.setCreateTime(LocalDateTime.now());

        boolean exists = smsRepository.existsByPhoneNumber(phoneNumber);
        if (exists) {
            // 如果新输入手机号存在则直接更新行
            smsRepository.updateSmsByPhoneNumber(sms);
        } else {
            // 不存在则插入新行
            smsRepository.save(sms);
        }
        return Result.success();
    }

    @Override
    public Result<SmsDTO> validateSmsCode(String phoneNumber, String code, String bizId) {
        if (!isValidPhoneNumber(phoneNumber) || code.isEmpty()) {
            return Result.error("校验失败，请检查输入");
        }

        Sms sms = smsRepository.findSmsByPhoneNumberAndBizId(phoneNumber, bizId);
        if (sms != null) {
            if (!passwordEncoder.matches(code, sms.getSmsCode())) {
                return Result.error("验证码错误");
            }

            String jwt = sms.getToken();
            try {
                JwtUtils.parseJwt(jwt);
            } catch (ExpiredJwtException e) {
                LOGGER.error("令牌已过有效期");
                return Result.error("验证码已过有效期");
            } catch (JwtException e) {
                LOGGER.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
                return Result.error("JWT解析错误");
            }

            SmsDTO smsDTO = new SmsDTO();
            smsDTO.setStatus(true);
            // 脱敏电话号码并设置到DTO对象中
            smsDTO.setPhoneNumber(maskPhoneNumber(sms.getPhoneNumber()));
            smsDTO.setBizId(sms.getBizId());

            smsRepository.deleteSmsByPhoneNumber(phoneNumber);
            return Result.success(smsDTO);
        }
        return Result.error("验证码错误");
    }

}