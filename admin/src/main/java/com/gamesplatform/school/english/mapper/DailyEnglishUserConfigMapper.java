package com.gamesplatform.school.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.school.english.entity.DailyEnglishUserConfig;
import org.apache.ibatis.annotations.Mapper;

/** 用户级每日英语配置数据访问组件。 */
@Mapper
public interface DailyEnglishUserConfigMapper extends BaseMapper<DailyEnglishUserConfig> {
}
