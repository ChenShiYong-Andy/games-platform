package com.gamesplatform.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 成就实体。
 */
@Data
@TableName("achievements")
public class Achievement {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 业务编码。
     */
    private String code;
    /**
     * 所属游戏编码。
     */
    private String gameCode;
    /**
     * 名称。
     */
    private String name;
    /**
     * 描述。
     */
    private String description;
    /**
     * 图标。
     */
    private String icon;
}
