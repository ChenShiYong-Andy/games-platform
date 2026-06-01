package com.gamesplatform.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.achievement.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成就数据访问接口。
 */
@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
}
