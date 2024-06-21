package com.zqc.itineraryweb.entity.login;

import lombok.Data;

@Data
public class LoginTokenVerifyRequest {
    private String loginToken;
    private String exID;
}