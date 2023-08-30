package com.zqc.itineraryweb.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sms {
    // id
    private int id;
    // 手机号
    private String phoneNumber;
    // 验证码
    private String smsCode;
    // 发送回执id
    private String bizId;
    // 发送日期时间
    private LocalDateTime sendTime;
    // 验证码过期时间
    private String expire;
    // 创建时间
    private LocalDateTime createTime;
}
