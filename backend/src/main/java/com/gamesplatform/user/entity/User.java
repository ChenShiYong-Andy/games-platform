package com.gamesplatform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private String email;
    private Integer level;
    private Integer totalPoints;
    private Integer loginStreak;
    private LocalDate lastLoginDate;
    private Integer totalClears;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
