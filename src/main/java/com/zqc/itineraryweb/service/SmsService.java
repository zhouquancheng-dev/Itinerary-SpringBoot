package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.Sms;

public interface SmsService {

    void insertSmsData(Sms sms);

    Sms querySmsData(String phoneNumber, String bizId);

    int querySmsByPhoneNumber(String phoneNumber);

    void updateSmsData(Sms sms);

    void deleteByPhoneNumber(String phoneNumber);

}