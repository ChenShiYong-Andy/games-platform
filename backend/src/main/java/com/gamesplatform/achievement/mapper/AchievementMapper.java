package com.gamesplatform.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.achievement.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
}
