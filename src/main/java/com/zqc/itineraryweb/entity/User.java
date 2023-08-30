package com.zqc.itineraryweb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private byte[] userId;
    private String username;
    private String password;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean locked;
}
