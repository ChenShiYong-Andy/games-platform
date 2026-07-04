package com.gamesplatform.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.pet.entity.PetColorConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物颜色配置数据访问接口。
 */
@Mapper
public interface PetColorConfigMapper extends BaseMapper<PetColorConfig> {
}
