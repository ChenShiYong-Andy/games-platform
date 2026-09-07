package com.gamesplatform.school.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.school.pet.entity.PetGrowthStageConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物成长阶段配置数据访问接口。
 */
@Mapper
public interface PetGrowthStageConfigMapper extends BaseMapper<PetGrowthStageConfig> {
}
