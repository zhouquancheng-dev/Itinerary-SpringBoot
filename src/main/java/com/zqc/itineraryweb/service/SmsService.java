package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.SmsDTO;

import java.time.LocalDateTime;

public interface SmsService {

    Result<Object> saveSmsData(String phoneNumber, String code, String bizId, LocalDateTime sendTime);

    Result<SmsDTO> validateSmsCode(String phoneNumber, String code, String bizId);

}