package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.Result;

public interface UserService {

    Result<Object> userLogin(String username, String password);

    Result<Object> autoLogin(String token);

    Result<Object> registerUser(String username, String password);

    Result<Object> logout(String token);

}