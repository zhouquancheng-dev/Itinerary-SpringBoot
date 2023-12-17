package com.zqc.itineraryweb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_sms")
public class Sms {
    @Id
    private int id;
    private String phoneNumber;
    private String smsCode;
    private String bizId;
    private LocalDate sendDate;
    private LocalDateTime createTime;
}