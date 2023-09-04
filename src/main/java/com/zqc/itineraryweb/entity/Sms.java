package com.zqc.itineraryweb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_sms")
public class Sms {
    // id
    @Id
    private int id;
    // 手机号
    private String phoneNumber;
    // 验证码
    private String smsCode;
    // 发送回执id
    private String bizId;
    // 发送日期时间
    private LocalDateTime sendTime;
    // 验证码token
    private String token;
    // 创建时间
    private LocalDateTime createTime;
}