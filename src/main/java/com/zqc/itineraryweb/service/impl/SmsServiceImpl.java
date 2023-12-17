package com.zqc.itineraryweb.service.impl;

import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.zqc.itineraryweb.dao.SmsRepository;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.Sms;
import com.zqc.itineraryweb.entity.SmsDTO;
import com.zqc.itineraryweb.service.SmsService;
import com.zqc.itineraryweb.utils.AliYunSMSClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static com.zqc.itineraryweb.utils.MaskUtils.maskPhoneNumber;
import static com.zqc.itineraryweb.utils.ValidationUtils.isValidPhoneNumber;
import static com.zqc.itineraryweb.utils.ValidationUtils.isValidSmsCode;

@Service
public class SmsServiceImpl implements SmsService {

    // 验证码的有效期，5分钟
    private static final long EXPIRATION_TIME = 5 * 60 * 1000L;

    private static final String INVALID_CODE_ERROR_MESSAGE = "验证码错误";

    private static final String EXPIRED_CODE_ERROR_MESSAGE = "验证码已过有效期";

    private static final String DELIVERED_MESSAGE = "DELIVERED";

    private final Random random = new Random();

    private final SmsRepository smsRepository;

    private final PasswordEncoder passwordEncoder;

    public SmsServiceImpl(SmsRepository smsRepository, PasswordEncoder passwordEncoder) {
        this.smsRepository = smsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Result<Object> sendSmsCode(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            return Result.error("发送失败，请检查手机号码");
        }

        int lowerBound = 1000;
        int upperBound = 10000;
        int randomCode = random.nextInt(upperBound - lowerBound) + lowerBound;

        SendSmsResponse response = AliYunSMSClient.sendSmsCode(phoneNumber, randomCode);

        if (response != null) {
            // 哈希加密验证码
            String hashedSMSCode = passwordEncoder.encode(String.valueOf(randomCode));

            Sms sms = new Sms();
            sms.setPhoneNumber(phoneNumber);
            sms.setSmsCode(hashedSMSCode);
            sms.setBizId(response.body.bizId);
            sms.setSendDate(LocalDate.now());
            sms.setCreateTime(LocalDateTime.now());

            boolean exists = smsRepository.existsByPhoneNumber(phoneNumber);
            if (exists) {
                // 如果发送的手机号已存在则直接更新对应行
                smsRepository.updateSmsByPhoneNumber(sms);
            } else {
                // 不存在则插入新行
                smsRepository.save(sms);
            }

            return Result.success(response.body);
        }
        return Result.error("发送失败");
    }

    @Override
    public Result<SmsDTO> validateSmsCode(String phoneNumber, String code, String bizId) {
        if (!isValidPhoneNumber(phoneNumber) || !isValidSmsCode(code)) {
            return Result.error("校验失败，请检查输入");
        }

        Sms sms = smsRepository.findSmsByPhoneNumberAndBizId(phoneNumber, bizId);
        if (sms == null || !passwordEncoder.matches(code, sms.getSmsCode())) {
            return Result.error(INVALID_CODE_ERROR_MESSAGE);
        }

        String sendDateFormatted = sms.getSendDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        QuerySendDetailsResponse response = AliYunSMSClient.querySendDetails(phoneNumber, bizId, sendDateFormatted);
        if (response != null && response.body.smsSendDetailDTOs.smsSendDetailDTO != null) {
            String phoneNum = response.body.smsSendDetailDTOs.smsSendDetailDTO.get(0).phoneNum;
            String errCode = response.body.smsSendDetailDTOs.smsSendDetailDTO.get(0).errCode;
            String receiveDateStr = response.body.smsSendDetailDTOs.smsSendDetailDTO.get(0).receiveDate;

            if (phoneNum.equals(phoneNumber) && errCode.equals(DELIVERED_MESSAGE)) {
                // 将 receiveDate 字符串解析为 LocalDateTime
                LocalDateTime receiveDate = LocalDateTime.parse(receiveDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                // 获取当前时间
                LocalDateTime currentTime = LocalDateTime.now();
                // 计算时间差（以秒为单位）
                long timeDifferenceInSeconds = Duration.between(receiveDate, currentTime).getSeconds();

                // 检查时间差是否在5分钟内（300秒）
                if (timeDifferenceInSeconds <= EXPIRATION_TIME / 1000) {
                    SmsDTO smsDTO = new SmsDTO();
                    smsDTO.setStatus(true);
                    smsDTO.setPhoneNumber(maskPhoneNumber(sms.getPhoneNumber()));
                    smsDTO.setBizId(sms.getBizId());

                    smsRepository.deleteSmsByPhoneNumber(phoneNumber);
                    return Result.success(smsDTO);
                } else {
                    return Result.error(EXPIRED_CODE_ERROR_MESSAGE);
                }
            }
        }
        return Result.error(INVALID_CODE_ERROR_MESSAGE);
    }

}