package com.gamesplatform.school.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.school.english.entity.DailyEnglishConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日英语配置数据访问组件。
 */
@Mapper
public interface DailyEnglishConfigMapper extends BaseMapper<DailyEnglishConfig> {
}
