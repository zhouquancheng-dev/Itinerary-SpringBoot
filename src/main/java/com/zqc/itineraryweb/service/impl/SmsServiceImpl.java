package com.zqc.itineraryweb.service.impl;

import com.zqc.itineraryweb.entity.Sms;
import com.zqc.itineraryweb.mappers.SmsMapper;
import com.zqc.itineraryweb.service.SmsService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SmsServiceImpl implements SmsService {

    private final SmsMapper smsMapper;

    public SmsServiceImpl(SmsMapper smsMapper) {
        this.smsMapper = smsMapper;
    }

    @Override
    public void insertSmsData(Sms sms) {
        sms.setCreateTime(LocalDateTime.now());
        smsMapper.insertSmsData(sms);
    }

    @Override
    public Sms querySmsData(String phoneNumber, String bizId) {
        return smsMapper.querySmsData(phoneNumber, bizId);
    }

    @Override
    public int querySmsByPhoneNumber(String phoneNumber) {
        return smsMapper.querySmsByPhoneNumber(phoneNumber);
    }

    @Override
    public void updateSmsData(Sms sms) {
        smsMapper.updateSmsData(sms);
    }

    @Override
    public void deleteByPhoneNumber(String phoneNumber) {
        smsMapper.deleteByPhoneNumber(phoneNumber);
    }

}
