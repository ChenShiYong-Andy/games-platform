package com.gamesplatform.school.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.school.english.entity.DailyEnglishPractice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日英语练习缓存数据访问组件。
 */
@Mapper
public interface DailyEnglishPracticeMapper extends BaseMapper<DailyEnglishPractice> {
}
