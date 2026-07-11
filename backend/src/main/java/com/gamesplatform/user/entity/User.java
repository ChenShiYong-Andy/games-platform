package com.gamesplatform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体。
 */
@Data
@TableName("users")
public class User {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 密码哈希。
     */
    private String passwordHash;
    /**
     * 管理密码哈希。
     */
    private String adminPasswordHash;
    /**
     * 昵称。
     */
    private String nickname;
    /**
     * 头像地址。
     */
    private String avatarUrl;
    /**
     * 邮箱地址。
     */
    private String email;
    /**
     * 用户等级。
     */
    private Integer level;
    /**
     * 累计积分。
     */
    private Integer totalPoints;
    /**
     * 连续登录天数。
     */
    private Integer loginStreak;
    /**
     * 最近登录日期。
     */
    private LocalDate lastLoginDate;
    /**
     * 累计通关次数。
     */
    private Integer totalClears;
    /**
     * 每日数独次数上限。
     */
    private Integer sudokuDailyLimit;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
