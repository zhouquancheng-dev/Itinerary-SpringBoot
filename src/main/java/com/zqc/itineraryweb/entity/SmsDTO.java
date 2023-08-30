package com.zqc.itineraryweb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsDTO {
    private boolean status;
    private String phoneNumber;
    private String bizId;
}
