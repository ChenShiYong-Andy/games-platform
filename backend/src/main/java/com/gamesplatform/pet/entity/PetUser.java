package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户宠物实体。
 */
@Data
@TableName("pet_user")
public class PetUser {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户 ID。
     */
    private Long userId;
    /**
     * 宠物类型。
     */
    private String petType;
    /**
     * 宠物颜色编码。
     */
    private String petColorCode;
    /**
     * 当前宠物资源标识。
     */
    private String petAssetKey;
    /**
     * 宠物名称。
     */
    private String petName;
    /**
     * 宠物等级。
     */
    private Integer level;
    /**
     * 当前成长阶段。
     */
    private Integer stageNo;
    /**
     * 成长值。
     */
    private Integer exp;
    /**
     * 饥饿值。
     */
    private Integer hunger;
    /**
     * 清洁值。
     */
    private Integer clean;
    /**
     * 快乐值。
     */
    private Integer happiness;
    /**
     * 体力值。
     */
    private Integer energy;
    /**
     * 爱心值。
     */
    private Integer loveValue;
    /**
     * 当前头饰。
     */
    private String currentHatCode;
    /**
     * 当前小床。
     */
    private String currentBedCode;
    /**
     * 当前房间主题。
     */
    private String currentRoomTheme;
    /**
     * 初始化时间。
     */
    private LocalDateTime initTime;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
