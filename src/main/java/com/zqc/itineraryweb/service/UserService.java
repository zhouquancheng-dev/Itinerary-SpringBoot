package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.Result;

public interface UserService {

    Result<Object> login(String username, String password);

    Result<Object> register(String username, String password, String confirmPassword);

    Result<Object> logout(String token);

    Result<Object> autoLogin(String token);

    Result<Object> phoneNumberLogin(String phoneNumber);

    Result<Object> checkRegistration(String username);

    Result<Object> resetPassword(String username, String newPassword, String confirmNewPassword);
}