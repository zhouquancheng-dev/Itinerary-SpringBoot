package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.SmsDTO;

public interface SmsService {

    Result<Object> sendSmsCode(String phoneNumber);

    Result<SmsDTO> validateSmsCode(String phoneNumber, String code, String bizId);

}