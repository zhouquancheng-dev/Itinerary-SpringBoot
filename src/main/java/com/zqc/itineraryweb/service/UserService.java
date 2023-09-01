package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.User;

public interface UserService {

    User login(String username);

    void register(User user);

    int getUserByToken(String username);

    int getHasByUsername(String username);

    void updateByTokenAndLastLoginAt(User user);

    void clearUserToken(String username, String token);

}
