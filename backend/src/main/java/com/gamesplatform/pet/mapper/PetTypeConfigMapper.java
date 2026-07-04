package com.gamesplatform.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.pet.entity.PetTypeConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物类型配置数据访问接口。
 */
@Mapper
public interface PetTypeConfigMapper extends BaseMapper<PetTypeConfig> {
}
