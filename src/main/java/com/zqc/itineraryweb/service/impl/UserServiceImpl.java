package com.zqc.itineraryweb.service.impl;

import com.zqc.itineraryweb.entity.User;
import com.zqc.itineraryweb.mappers.UserMapper;
import com.zqc.itineraryweb.service.UserService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User login(String username) {
        return userMapper.getByUser(username);
    }

    @Override
    public void register(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setLastLoginAt(now);

        userMapper.insertUser(user);
    }

    @Override
    public int getUserByToken(String username) {
        return userMapper.getUserByToken(username);
    }

    @Override
    public int getHasByUsername(String username) {
        return userMapper.getHasByUsername(username);
    }

    @Override
    public void updateByTokenAndLastLoginAt(User user) {
        userMapper.updateByTokenAndLastLoginAt(user);
    }

    @Override
    public int clearUserToken(String username, String token) {
        return userMapper.clearUserToken(username, token);
    }

}
