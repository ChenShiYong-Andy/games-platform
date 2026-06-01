package com.gamesplatform.zoo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.zoo.entity.ZooGame;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动物园游戏记录数据访问接口。
 */
@Mapper
public interface ZooGameMapper extends BaseMapper<ZooGame> {
}
