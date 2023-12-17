package com.zqc.itineraryweb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_user")
public class User {
    @Id
    private byte[] userId;
    private String username;
    private String password;
    private LocalDateTime lastLoginAt;
    private boolean locked;
    private LocalDateTime createdAt;
}